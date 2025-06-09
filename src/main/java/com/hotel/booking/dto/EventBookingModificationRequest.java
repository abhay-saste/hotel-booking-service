package com.hotel.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EventBookingModificationRequest(
        @NotNull @FutureOrPresent LocalDate eventDate,
        @Min(1) int attendees,
        String bookingType
) implements BookingModificationRequest {
    @Override
    public String getBookingType() {
        return bookingType;
    }
}