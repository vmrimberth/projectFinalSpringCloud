package com.rvmsoft.moviebooking.movie.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta con los datos de un producto.
 *
 * @param id             Identificador único de screening
 * @param movieId        Pelicula referenciada
 * @param showAt         Genero de la pelicula
 * @param totalSeats     Tiempo de duracion
 * @param availableSeats Rating de la pelicula
 * @param active         Si la pelicula está activo
 */
public record ScreeningResponse(
        UUID id,
        UUID movieId,
        String movieTitle,
        String room,
        LocalDateTime showAt,
        Integer totalSeats,
        Integer availableSeats,
        boolean active
) {}
