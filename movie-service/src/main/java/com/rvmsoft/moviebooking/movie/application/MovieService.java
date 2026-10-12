package com.rvmsoft.moviebooking.movie.application;

import com.rvmsoft.moviebooking.movie.api.dto.MovieRequest;
import com.rvmsoft.moviebooking.movie.api.dto.MovieResponse;
import com.rvmsoft.moviebooking.movie.api.dto.MovieUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface MovieService {
    MovieResponse create(MovieRequest request);

    List<MovieResponse> findAll();

    MovieResponse findById(UUID id);

    MovieResponse update(UUID id, MovieUpdateRequest request);

    void delete(UUID id);

    MovieResponse activate(UUID id);

    MovieResponse deactivate(UUID id);
}
