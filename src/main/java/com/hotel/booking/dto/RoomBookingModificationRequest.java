package com.hotel.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record RoomBookingModificationRequest(
        @NotNull @FutureOrPresent LocalDate checkIn,
        @NotNull @Future LocalDate checkOut,
        String bookingType,
        Set<String> decorators
) implements BookingModificationRequest {
    @Override
    public String getBookingType() {
        return bookingType;
    }
}