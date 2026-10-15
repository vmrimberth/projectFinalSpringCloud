package com.rvmsoft.moviebooking.booking.api;

import com.rvmsoft.moviebooking.booking.api.dto.BookingResponse;
import com.rvmsoft.moviebooking.booking.api.dto.CreateBookingRequest;
import com.rvmsoft.moviebooking.booking.application.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Bookings Service", description = "Endpoints para Bookings Service")
@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final Logger logger= LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(
            summary = "Crear Bookings",
            description = "Crear nuevo registro de Bookings",
            security = @SecurityRequirement(name = "bearerAuth")
    )@ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bookings Reserva creada y CONFIRMED con header Location",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class)),
                    headers = @Header(
                        name = "Location", description = "URL de la Bookings recien creada. Ej: /api/v1/bookings/{id}", example = "/api/v1/bookings/{id}",
                        schema = @Schema(type = "string", format = "uri")
                    )),
            @ApiResponse(responseCode = "400", description = "Validación de campos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Bookings Función sin asientos disponibles",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "502", description = "Fallo de infraestructura contra movie-service tras los reintentos (compensación ejecutada)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        logger.info("Creating booking for screening: {}", request.screeningId());
        BookingResponse response = bookingService.createBooking(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Encontrar Booking por id",
            description = "Retorna un objeto Bookings en base al parametro idMovie",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings Reserva encontrada",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Bookings Reserva inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBooking(id));
    }

    @Operation(
            summary = "Confirmar booking",
            description = "Confirma el estado Pending de un booking a Confirmado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings Reserva confirmada (CONFIRM)",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Bookings Reserva inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Bookings la reserva ya estaba CANCELLED",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    @Operation(
            summary = "Cancelar booking",
            description = "Cancela el estado Pending de un booking a Cancelado y se procede a liberar los asientos reservados",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings Reserva cancelada (CANCELLED)",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Bookings Reserva inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Bookings la reserva ya estaba CANCELLED",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}
