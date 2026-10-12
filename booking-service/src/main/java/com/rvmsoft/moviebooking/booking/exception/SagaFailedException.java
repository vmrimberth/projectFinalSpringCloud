package com.rvmsoft.moviebooking.booking.exception;

import org.springframework.http.HttpStatus;

/**
 * Fallo de infraestructura del SAGA (movie-service inaccesible o con error 5xx)
 * después de agotar los reintentos. Se responde con ProblemDetail RFC 7807.
 */
public class SagaFailedException extends RuntimeException {

    public SagaFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpStatus getStatus() {
        return HttpStatus.BAD_GATEWAY;
    }
}
