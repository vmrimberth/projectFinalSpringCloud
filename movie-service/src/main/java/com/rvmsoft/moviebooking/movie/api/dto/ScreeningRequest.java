package com.rvmsoft.moviebooking.movie.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request con los datos de un Screening.
 *
 * @param movieId       Relacion con la pelicula
 * @param room          Sala
 * @param showAt        Fecha y Hora
 * @param totalSeats    capacidad fija
 */
public record ScreeningRequest(
        @NotNull(message = "Id is required")
        UUID movieId,
        @NotBlank(message = "Room is required")
        String room,
        @NotNull(message = "ShowAt is required")
        @Future
        LocalDateTime showAt,
        @NotNull(message = "TotalSeats is required")
        @Positive
        Integer totalSeats
) {}
