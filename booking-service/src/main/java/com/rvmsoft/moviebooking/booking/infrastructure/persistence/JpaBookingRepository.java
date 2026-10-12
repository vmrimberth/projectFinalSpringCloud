package com.rvmsoft.moviebooking.booking.infrastructure.persistence;

import com.rvmsoft.moviebooking.booking.domain.Booking;
import com.rvmsoft.moviebooking.booking.domain.BookingRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaBookingRepository extends JpaRepository<Booking, UUID>, BookingRepository {
}
