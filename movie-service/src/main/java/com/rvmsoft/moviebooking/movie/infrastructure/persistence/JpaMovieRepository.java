package com.rvmsoft.moviebooking.movie.infrastructure.persistence;

import com.rvmsoft.moviebooking.movie.domain.Movie;
import com.rvmsoft.moviebooking.movie.domain.MovieRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaMovieRepository extends JpaRepository<Movie, UUID>, MovieRepository {
}
