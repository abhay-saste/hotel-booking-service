package com.hotel.booking.dto;

import com.hotel.booking.constants.BookingStatus;

public record BookingResponse(Long bookingId, Long userId, BookingStatus status, double totalPrice, String detailedDescription, String message) {}