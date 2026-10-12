package com.rvmsoft.moviebooking.movie.api;

import com.rvmsoft.moviebooking.movie.api.dto.ScreeningRequest;
import com.rvmsoft.moviebooking.movie.api.dto.ScreeningResponse;
import com.rvmsoft.moviebooking.movie.api.dto.WelcomeResponse;
import com.rvmsoft.moviebooking.movie.application.ScreeningService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador HTTP de Screening de movies.
 *
 *   POST   /screenings               Crea un pelicula              → 201 Created
 *   GET    /screenings               Lista todos las peliculas      → 200 OK
 *   GET    /screenings/{id}          Obtiene una pelicula            → 200 OK | 404
 *   PATCH  /screenings/{id}/reserve  Decrementa availableSeats             → 200 OK | 404 | 422
 *   PATCH  /screenings/{id}/release  Incrementa availableSeats (compensación) → 200 OK | 404 | 422
 *
 * SRP: este controller solo se ocupa del mapeo HTTP.
 * Toda la lógica de negocio vive en ScreeningService.
 */
@RestController
@RequestMapping("/screenings")
public class ScreeningController {
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);

    private final ScreeningService screeningService;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${spring.boot.version:4.0.3}")
    private String springBootVersion;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    // ── Módulo 01 ─────────────────────────────────────────────────────────────

    @GetMapping("/hello")
    public ResponseEntity<WelcomeResponse> hello() {
        return ResponseEntity.ok(new WelcomeResponse(
                serviceName,
                "0.0.1-SNAPSHOT",
                Runtime.version().toString(),
                springBootVersion,
                "Hello from Screening Service! Spring Boot 4 + Java 25 running successfully."
        ));
    }

    // ── Módulo 02: CRUD ───────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ScreeningResponse> create(@Valid @RequestBody ScreeningRequest request) {
        ScreeningResponse response = screeningService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ScreeningResponse>> findAll(@RequestHeader(value = "X-Api-Version", required = false) String apiVersion) {
        logger.info("Fetching all screening in API version: {}", apiVersion);
        return ResponseEntity.ok(screeningService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScreeningResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(screeningService.findById(id));
    }

    // ── Módulo 03: DDD — operaciones de dominio ───────────────────────────────

    @PatchMapping("/{id}/reserve")
    public ResponseEntity<ScreeningResponse> reserveSeat(@PathVariable UUID id) {
        return ResponseEntity.ok(screeningService.reserveSeat(id));
    }

    @PatchMapping("/{id}/release")
    public ResponseEntity<ScreeningResponse> releaseSeat(@PathVariable UUID id) {
        return ResponseEntity.ok(screeningService.releaseSeat(id));
    }
}
