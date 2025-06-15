package com.hotel.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record EventBookingRequest(
        @NotNull Long userId,
        @NotNull Long hotelId,
        @NotNull String eventName,
        @NotNull @FutureOrPresent LocalDate eventDate,
        @Min(1) int attendees,
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