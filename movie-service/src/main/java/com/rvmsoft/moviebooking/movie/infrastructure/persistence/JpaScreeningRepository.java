package com.rvmsoft.moviebooking.movie.infrastructure.persistence;

import com.rvmsoft.moviebooking.movie.domain.Screening;
import com.rvmsoft.moviebooking.movie.domain.ScreeningRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaScreeningRepository extends JpaRepository<Screening, UUID>, ScreeningRepository {
}
