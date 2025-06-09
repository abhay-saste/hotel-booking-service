package com.hotel.booking.strategy;

import com.hotel.booking.dto.BookingDetailsResponse;
import com.hotel.booking.dto.BookingModificationRequest;
import com.hotel.booking.dto.BookingRequest;
import com.hotel.booking.dto.BookingResponse;

public interface BookingStrategy {
    BookingResponse book(BookingRequest request);
    BookingResponse modify(Long bookingId, BookingModificationRequest request);
    BookingResponse cancel(Long bookingId);
    BookingDetailsResponse findById(Long bookingId);
    String getStrategyType();
}