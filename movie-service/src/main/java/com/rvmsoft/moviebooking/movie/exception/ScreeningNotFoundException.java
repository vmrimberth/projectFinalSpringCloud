package com.rvmsoft.moviebooking.movie.exception;

import java.util.UUID;

public class ScreeningNotFoundException extends RuntimeException {
    public ScreeningNotFoundException(UUID id) {
        super("Screening not found with id: " + id);
    }
}
