package com.rvmsoft.moviebooking.movie.api;

import com.rvmsoft.moviebooking.movie.api.dto.*;
import com.rvmsoft.moviebooking.movie.application.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
 * Adaptador HTTP del catálogo de movies.
 *
 * Recursos expuestos (context-path /api/v1 desde application.yml):
 *
 *   GET    /movies/hello             Hello world
 *   POST   /movies                   Crea un pelicula              → 201 Created
 *   GET    /movies                   Lista todos las peliculas      → 200 OK
 *   GET    /movies/{id}              Obtiene una pelicula            → 200 OK | 404
 *   PUT    /movies/{id}              Actualiza datos de la pelicula   → 200 OK | 404
 *   DELETE /movies/{id}              Elimina una pelicula            → 204 No Content | 404
 *   PATCH  /movies/{id}/activate     Activa la pelicula          → 200 OK | 404
 *   PATCH  /movies/{id}/deactivate   Desactiva la pelicula       → 200 OK | 404
 *
 * SRP: este controller solo se ocupa del mapeo HTTP.
 * Toda la lógica de negocio vive en ProductService.
 */

@Tag(name = "Movies Service", description = "Endpoints para Movies Service")
@RestController
@RequestMapping("/movies")
public class MovieController {
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);

    private final MovieService movieService;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${spring.boot.version:4.0.3}")
    private String springBootVersion;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // ── Módulo 01 ─────────────────────────────────────────────────────────────

    @GetMapping("/hello")
    public ResponseEntity<WelcomeResponse> hello() {
        return ResponseEntity.ok(new WelcomeResponse(
                serviceName,
                "0.0.1-SNAPSHOT",
                Runtime.version().toString(),
                springBootVersion,
                "Hello from Movie Service! Spring Boot 4 + Java 25 running successfully."
        ));
    }

    // ── Módulo 02: CRUD ───────────────────────────────────────────────────────

    @Operation(
            summary = "Crear Movies",
            description = "Crear nuevo registro de Movies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movie creada",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validación de campos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Violación de una regla de negocio",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<MovieResponse> create(@Valid @RequestBody MovieRequest request) {
        MovieResponse response = movieService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Encontrar todas las Movies",
            description = "Retorna el listado de todas las movies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de Movies",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<MovieResponse>> findAll(@RequestHeader(value = "X-Api-Version", required = false) String apiVersion) {
        logger.info("Fetching all movies in API version: {}", apiVersion);
        return ResponseEntity.ok(movieService.findAll());
    }

    @Operation(
            summary = "Encontrar Movie por id",
            description = "Retorna un objeto Movie en base al parametro idMovie",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie encontrada",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "404", description = "Movie inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(movieService.findById(id));
    }

    @Operation(
            summary = "Actualizar Movie",
            description = "Actualiza los datos de una Movie",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie actualizada",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validación de campos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Movie inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody MovieUpdateRequest request) {
        return ResponseEntity.ok(movieService.update(id, request));
    }

    @Operation(
            summary = "Eliminar Movie",
            description = "Elimina el registro de una Movie",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Movie eliminada", content = @Content),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Movie inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Módulo 03: DDD — operaciones de dominio ───────────────────────────────

    @Operation(
            summary = "Activar Movie",
            description = "Actualiza el estado a activo de una Movie",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie activada",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Movie inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{id}/activate")
    public ResponseEntity<MovieResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(movieService.activate(id));
    }

    @Operation(
            summary = "Inactivar Movie",
            description = "Actualiza el estado a inactivo de una Movie",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie desactivada",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Movie inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<MovieResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(movieService.deactivate(id));
    }
}
