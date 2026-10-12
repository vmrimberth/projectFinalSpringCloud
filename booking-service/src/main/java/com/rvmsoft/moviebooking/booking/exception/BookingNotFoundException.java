package com.rvmsoft.moviebooking.booking.exception;

import java.util.UUID;

public class BookingNotFoundException extends RuntimeException {
  public BookingNotFoundException(UUID id) {
      super("Movie not found with id: " + id);
  }
}
