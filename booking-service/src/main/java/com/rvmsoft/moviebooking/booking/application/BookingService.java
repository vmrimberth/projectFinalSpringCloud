package com.rvmsoft.moviebooking.booking.application;

import com.rvmsoft.moviebooking.booking.api.dto.BookingResponse;
import com.rvmsoft.moviebooking.booking.api.dto.CreateBookingRequest;

import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(CreateBookingRequest request);
    BookingResponse getBooking(UUID id);
    BookingResponse confirmBooking(UUID id);
    BookingResponse cancelBooking(UUID id);
}
