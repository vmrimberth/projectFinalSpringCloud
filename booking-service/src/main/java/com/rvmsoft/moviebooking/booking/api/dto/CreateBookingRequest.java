package com.rvmsoft.moviebooking.booking.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull
        UUID screeningId,

        @NotNull
        UUID customerId,

        @NotNull
        @Positive
        BigDecimal totalAmount
) {}
