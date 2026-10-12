package com.rvmsoft.moviebooking.movie.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movies")
@EntityListeners(AuditingEntityListener.class)
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String director;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    @Column(name = "duration_min")
    private Integer durationMin;

    private String rating;

    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Constructor requerido por JPA para instanciar via reflexión. */
    public Movie() {}

    /**
     * Factory method: crea una movie con estado inicial válido.
     *
     * Preferir este método sobre el constructor público para garantizar
     * que las movies siempre se crean con todos los campos requeridos.
     */
    public static Movie create(String title, String director, Genre genre,
                                 Integer durationMin, String rating) {
        var movie = new Movie();
        movie.title = title;
        movie.director = director;
        movie.genre = genre;
        movie.durationMin = durationMin;
        movie.rating = rating;
        return movie;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID getId()             { return id; }
    public String getTitle()        { return title; }
    public String getDirector()     { return director; }
    public Genre getGenre()         { return genre; }
    public Integer getDurationMin() { return durationMin; }
    public String getRating()       { return rating; }
    public boolean isActive()       { return active; }
    public Instant getCreatedAt()   { return createdAt; }
    public Instant getUpdatedAt()   { return updatedAt; }

    // ── Setters para actualización de campos simples ───────────────────────────

    public void setTitle(String title)                  { this.title = title; }
    public void setDirector(String director)            { this.director = director; }
    public void setGenre(Genre genre)                   { this.genre = genre; }
    public void setDurationMin(Integer durationMin)     { this.durationMin = durationMin; }
    public void setRating(String rating)                { this.rating = rating; }

    // ── Métodos de negocio ────────────────────────────────────────────────────


    /** Activa la pelicula para que aparezca en el screening. */
    public void activate() {
        this.active = true;
    }

    /** Desactiva la pelicula sin eliminarlo del sistema. */
    public void deactivate() {
        this.active = false;
    }
}
