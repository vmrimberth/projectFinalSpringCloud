package com.rvmsoft.moviebooking.movie.exception;

import java.util.UUID;

public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(UUID id) {
        super("Movie not found with id: " + id);
    }
}
