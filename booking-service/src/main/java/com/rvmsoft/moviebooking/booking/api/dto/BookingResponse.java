package com.rvmsoft.moviebooking.booking.api.dto;

import com.rvmsoft.moviebooking.booking.domain.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID screeningId,
        String movieTitle,
        UUID customerId,
        BookingStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant updatedAt
) {}
