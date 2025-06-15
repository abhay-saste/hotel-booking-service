package com.hotel.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record RoomBookingRequest(
        @NotNull Long userId,
        @NotNull Long hotelId,
        @NotNull Long roomId,
        @NotNull @FutureOrPresent LocalDate checkIn,
        @NotNull @Future LocalDate checkOut,
        String bookingType,
        Set<String> decorators
) implements BookingRequest {
    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public String getBookingType() {
        return bookingType;
    }

    @Override
    public Long getHotelId() {
        return hotelId;
    }
}
