package com.rvmsoft.moviebooking.movie.domain;

import com.rvmsoft.moviebooking.movie.exception.NoSeatsAvailableException;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "screenings")
@EntityListeners(AuditingEntityListener.class)
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID movieId;

    @Column(nullable = false)
    private String room;

    @Column(nullable = false)
    private LocalDateTime showAt;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false)
    private boolean active = true;

    public Screening(){}

    public static Screening create(UUID movieId, String room, LocalDateTime showAt,
                                   Integer totalSeats) {
        var screening = new Screening();
        screening.movieId = movieId;
        screening.room = room;
        screening.showAt = showAt;
        screening.totalSeats = totalSeats;
        screening.availableSeats = totalSeats;
        return screening;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID getId()                 { return id; }
    public UUID getMovieId()            { return movieId; }
    public String getRoom()             { return room; }
    public LocalDateTime getShowAt()      { return showAt; }
    public Integer getTotalSeats() { return totalSeats; }
    public Integer getAvailableSeats()      { return availableSeats; }
    public boolean isActive()       { return active; }

    // ── Setters para actualización de campos simples ───────────────────────────

    public void setMovieId(UUID movieId)                    { this.movieId = movieId; }
    public void setRoom(String room)                        { this.room = room; }
    public void setShowAt(LocalDateTime showAt)             { this.showAt = showAt; }
    public void setTotalSeats(Integer totalSeats)           { this.totalSeats = totalSeats; }
    public void setAvailableSeats(Integer availableSeats)   { this.availableSeats = availableSeats; }

    // ── Métodos de negocio ────────────────────────────────────────────────────

    public void reserve() {
        if (this.availableSeats == null || this.availableSeats <= 0) {
            throw new NoSeatsAvailableException("Función sin asientos disponibles");
        }
        this.availableSeats--;
    }

    public void release() {
        if (this.availableSeats != null && this.availableSeats < this.totalSeats) {
            this.availableSeats++;
        }
    }

    /** Activa la pelicula para que aparezca en el screening. */
    public void activate() {
        this.active = true;
    }

    /** Desactiva la pelicula sin eliminarlo del sistema. */
    public void deactivate() {
        this.active = false;
    }
}
