package com.rvmsoft.moviebooking.movie.api.dto;

/**
 * Respuesta del endpoint de bienvenida.
 *
 * @param service       Nombre del servicio
 * @param version       Versión del servicio
 * @param javaVersion   Versión de Java en tiempo de ejecución
 * @param springBoot    Versión de Spring Boot
 * @param message       Mensaje de bienvenida
 */
public record WelcomeResponse(
        String service,
        String version,
        String javaVersion,
        String springBoot,
        String message
) {}
