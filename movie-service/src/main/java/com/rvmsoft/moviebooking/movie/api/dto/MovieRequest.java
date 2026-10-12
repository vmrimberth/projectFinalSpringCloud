package com.rvmsoft.moviebooking.movie.api.dto;

import com.rvmsoft.moviebooking.movie.domain.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


/**
 * Request para crear una pelicula.
 *
 *
 * @param title          Titulo de la pelicula (requerido)
 * @param director       Director (requerido)
 * @param genre          Genre (requerido, valores: ACTION, COMEDY, DRAMA, HORROR,...)
 * @param durationMin    Duracion de la pelicula(no negativo)
 * @param rating         Rating
 */

public record MovieRequest(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Director is required")
        String director,
        @NotNull(message = "Genre is required")
        Genre genre,
        @Positive
        Integer durationMin,
        String rating
) {}
