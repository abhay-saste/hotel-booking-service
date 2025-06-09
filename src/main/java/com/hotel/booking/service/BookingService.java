package com.hotel.booking.service;

import com.hotel.booking.dto.*;

import java.time.LocalDate;
import java.util.List;


public interface BookingService {

    BookingResponse processBooking(BookingRequest request);

    BookingDetailsResponse findBookingById(Long bookingId, String bookingType);

    List<AvailabilityResponse> findAvailableRooms(Long hotelId, LocalDate checkIn, LocalDate checkOut, int minCapacity);

    BookingResponse modifyBooking(Long bookingId, BookingModificationRequest request);

    BookingResponse cancelBooking(Long bookingId, String bookingType);
}