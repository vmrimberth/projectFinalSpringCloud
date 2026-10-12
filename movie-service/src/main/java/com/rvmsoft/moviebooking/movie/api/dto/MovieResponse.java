package com.rvmsoft.moviebooking.movie.api.dto;

import com.rvmsoft.moviebooking.movie.domain.Genre;

import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta con los datos de un producto.
 *
 * @param id            Identificador único de la pelicula
 * @param title         Titulo de la pelicula
 * @param director      Director de la pelicula
 * @param genre         Genero de la pelicula
 * @param durationMin   Tiempo de duracion
 * @param rating        Rating de la pelicula
 * @param active        Si la pelicula está activo
 * @param createdAt     Fecha de creación (UTC)
 * @param updatedAt     Fecha de última modificación (UTC)
 */
public record MovieResponse(
        UUID id,
        String title,
        String director,
        Genre genre,
        Integer durationMin,
        String rating,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
