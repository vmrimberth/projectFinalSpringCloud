package com.rvmsoft.moviebooking.movie.application;

import com.rvmsoft.moviebooking.movie.api.dto.ScreeningRequest;
import com.rvmsoft.moviebooking.movie.api.dto.ScreeningResponse;

import java.util.List;
import java.util.UUID;

public interface ScreeningService {
    ScreeningResponse create(ScreeningRequest request);

    List<ScreeningResponse> findAll();

    ScreeningResponse findById(UUID id);

    ScreeningResponse reserveSeat(UUID id);

    ScreeningResponse releaseSeat(UUID id);
}
