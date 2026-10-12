package com.rvmsoft.moviebooking.movie.application.impl;

import com.rvmsoft.moviebooking.movie.api.dto.ScreeningRequest;
import com.rvmsoft.moviebooking.movie.api.dto.ScreeningResponse;
import com.rvmsoft.moviebooking.movie.application.MovieService;
import com.rvmsoft.moviebooking.movie.application.ScreeningService;
import com.rvmsoft.moviebooking.movie.domain.Screening;
import com.rvmsoft.moviebooking.movie.domain.ScreeningRepository;
import com.rvmsoft.moviebooking.movie.exception.MovieNotFoundException;
import com.rvmsoft.moviebooking.movie.exception.ScreeningNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ScreeningServiceImpl implements ScreeningService {
    private final ScreeningRepository repository;
    private final MovieService movieService;

    public ScreeningServiceImpl(ScreeningRepository repository, MovieService movieService) {
        this.repository = repository;
        this.movieService = movieService;
    }

    @Override
    @Transactional
    public ScreeningResponse create(ScreeningRequest request) {
        var screening = Screening.create(
                request.movieId(),
                request.room(),
                request.showAt(),
                request.totalSeats()
        );
        var saved = repository.save(screening);

        return toResponse(saved);
    }

    @Override
    public List<ScreeningResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ScreeningResponse findById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }

    @Override
    @Transactional
    public ScreeningResponse reserveSeat(UUID id) {
        var screening = repository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException(id));
        screening.reserve();
        var saved = repository.save(screening);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ScreeningResponse releaseSeat(UUID id) {
        var screening = repository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException(id));
        screening.release();
        var saved = repository.save(screening);

        return toResponse(saved);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ScreeningResponse toResponse(Screening screening) {

        return new ScreeningResponse(
                screening.getId(),
                screening.getMovieId(),
                movieService.findById(screening.getMovieId()).title(),
                screening.getRoom(),
                screening.getShowAt(),
                screening.getTotalSeats(),
                screening.getAvailableSeats(),
                screening.isActive()
        );
    }
}
