package com.rvmsoft.moviebooking.booking.infrastructure.client.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScreeningResponse(
        UUID id,
        UUID movieId,
        String movieTitle,
        LocalDateTime showAt,
        Integer totalSeats,
        Integer availableSeats,
        boolean active
) {
}
