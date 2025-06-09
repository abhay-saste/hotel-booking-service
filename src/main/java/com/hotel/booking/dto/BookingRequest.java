package com.hotel.booking.dto;

public interface BookingRequest {
    Long getUserId();
    String getBookingType();
    Long getHotelId();
}