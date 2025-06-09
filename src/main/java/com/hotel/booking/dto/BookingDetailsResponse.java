package com.hotel.booking.dto;

import com.hotel.booking.constants.BookingStatus;

public record BookingDetailsResponse(Long id, BookingStatus status, Long userId, String userName, Long hotelId, String hotelName, Object details) {}
