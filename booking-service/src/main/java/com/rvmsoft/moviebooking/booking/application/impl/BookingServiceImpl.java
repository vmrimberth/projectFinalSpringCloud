package com.rvmsoft.moviebooking.booking.application.impl;

import com.rvmsoft.moviebooking.booking.api.dto.BookingResponse;
import com.rvmsoft.moviebooking.booking.api.dto.CreateBookingRequest;
import com.rvmsoft.moviebooking.booking.application.BookingService;
import com.rvmsoft.moviebooking.booking.domain.Booking;
import com.rvmsoft.moviebooking.booking.domain.BookingRepository;
import com.rvmsoft.moviebooking.booking.exception.BookingNotFoundException;
import com.rvmsoft.moviebooking.booking.exception.InvalidBookingStateException;
import com.rvmsoft.moviebooking.booking.exception.SagaFailedException;
import com.rvmsoft.moviebooking.booking.infrastructure.client.MovieServiceClient;
import com.rvmsoft.moviebooking.booking.infrastructure.client.dto.ScreeningResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final MovieServiceClient movieServiceClient;

    /**
     * SAGA de reserva (4 pasos).
     *
     * No lleva @Transactional: cada save corre en su propia transacción local (Spring Data
     * JPA), que se confirma al terminar la llamada. Así la compensación CANCELLED sobrevive
     * aunque el request falle después. SAGA = transacciones locales + compensaciones,
     * nunca una transacción global que se revierta entera.
     */
    public BookingServiceImpl(BookingRepository bookingRepository, MovieServiceClient movieServiceClient) {
        this.bookingRepository = bookingRepository;
        this.movieServiceClient = movieServiceClient;
    }

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {
        // Paso 1: verificar función disponible
        ScreeningResponse screening;
        try {
            screening = movieServiceClient.getScreening(request.screeningId());
        } catch (Exception e) {
            throw new InvalidBookingStateException(
                    "No se pudo verificar la función " + request.screeningId() + ": " + e.getMessage());
        }

        //  SAGA: movie-service verifica asientos
        if (screening.availableSeats() == null || screening.availableSeats() <= 0) {
            throw new InvalidBookingStateException("Función sin asientos disponibles");
        }

        Booking booking = Booking.create(
                request.customerId(),
                request.screeningId(),
                screening.movieTitle());

        // Paso 2: crear booking en estado PENDING
        Booking savedBooking = bookingRepository.save(booking);
        UUID bookingId = savedBooking.getId();
        boolean seatReserved = false;
        try {
            // Paso 3: reservar asiento en movie-service (con @Retry)
            movieServiceClient.reserveSeat(savedBooking.getScreeningId());
            seatReserved = true; // ← se actualiza DESPUÉS de completar el paso

            // Paso 4: confirmar booking
            savedBooking.confirm();
            return toResponse(bookingRepository.save(savedBooking));
        }catch (RuntimeException e) {
            log.error("SAGA de reserva falló. Ejecutando compensaciones. Error: {}", e.getMessage());
            if (seatReserved) {
                compensate("release_seat", () -> movieServiceClient.releaseSeat(savedBooking.getScreeningId()));
            }
            if (bookingId != null) {
                compensate("cancel_booking", () -> {
                    savedBooking.cancel();
                    bookingRepository.save(savedBooking);});
            }
            //throw translate(e);
        }
        return toResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse getBooking(UUID id) {
        return bookingRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }

    @Override
    public BookingResponse confirmBooking(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        booking.confirm();
        return toResponse(bookingRepository.save(booking));
    }

    /**
     * SAGA inverso de cancelación. El estado CANCELLED se persiste ANTES de la llamada
     * remota: si liberar el asiento falla, se loguea y NO se revierte (best-effort).
     */
    @Override
    public BookingResponse cancelBooking(UUID id) {
        // Paso 1: verificar que existe (404) y puede cancelarse (422 si ya estaba CANCELLED)
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
        booking.cancel();

        // Paso 2: liberar asiento en movie-service (best-effort, absorbido en el adapter)
        movieServiceClient.releaseSeat(booking.getScreeningId());


        // Paso 3: actualizar booking a CANCELLED
        return toResponse(bookingRepository.save(booking));
    }

    private void compensate(String step, Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            log.warn("Compensación '{}' falló (best-effort, no se revierte): {}", step, ex.getMessage());
        }
    }

    private RuntimeException translate(RuntimeException e) {
        if (e instanceof InvalidBookingStateException) {
            return e;
        }
        if (e instanceof HttpClientErrorException clientError) {
            // 422 de movie-service (sin asientos) u otro 4xx: violación de invariante.
            return new InvalidBookingStateException("movie-service rechazó la reserva ("
                    + clientError.getStatusCode().value() + "): " + clientError.getMessage());
        }
        // Fallo de red / 5xx / servicio inaccesible tras agotar los reintentos.
        return new SagaFailedException("SAGA abortado tras reintentos: no se pudo contactar con movie-service. "
                + e.getMessage(), e);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getScreeningId(),
                booking.getMovieTitle(),
                booking.getCustomerId(),
                booking.getStatus(),
                booking.getTotalAmount(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
