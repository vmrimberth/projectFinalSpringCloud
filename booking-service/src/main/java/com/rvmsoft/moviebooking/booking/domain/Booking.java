package com.rvmsoft.moviebooking.booking.domain;

import com.rvmsoft.moviebooking.booking.exception.InvalidBookingStateException;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID screeningId;

    @Column(nullable = false)
    private String movieTitle;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Booking(){}

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID getId()             { return id; }
    public UUID getScreeningId()    { return screeningId; }
    public String getMovieTitle()     { return movieTitle; }
    public UUID getCustomerId()        { return customerId; }
    public BookingStatus getStatus() { return status; }
    public BigDecimal getTotalAmount()       { return totalAmount; }
    public Instant getCreatedAt()   { return createdAt; }
    public Instant getUpdatedAt()   { return updatedAt; }



    public static Booking create(UUID customerId, UUID screeningId, String movieTitle) {
        Booking booking = new Booking();
        booking.customerId = customerId;
        booking.screeningId = screeningId;
        booking.status = BookingStatus.PENDING;
        booking.movieTitle = movieTitle;
        booking.totalAmount = BigDecimal.ZERO;
        booking.createdAt = Instant.now();
        booking.updatedAt = Instant.now();
        return booking;
    }

    public void cancel() {
        if (this.status == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Reserva ya se encuentra cancelada");
        }
        this.status = BookingStatus.CANCELLED;
    }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }
}
