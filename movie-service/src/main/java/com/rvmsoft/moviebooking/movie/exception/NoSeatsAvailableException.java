package com.rvmsoft.moviebooking.movie.exception;

import java.util.UUID;

public class NoSeatsAvailableException extends RuntimeException {
    public NoSeatsAvailableException(String mensaje) {
        super(mensaje);
    }
}
