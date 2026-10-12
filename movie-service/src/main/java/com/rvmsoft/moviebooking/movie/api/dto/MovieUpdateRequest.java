package com.rvmsoft.moviebooking.movie.api.dto;

import com.rvmsoft.moviebooking.movie.domain.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MovieUpdateRequest(
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
