package com.hotel.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record EventBookingModificationRequest(
        @NotNull @FutureOrPresent LocalDate eventDate,
        @Min(1) int attendees,
        String bookingType,
        Set<String> decorators
) implements BookingModificationRequest {
    @Override
    public String getBookingType() {
        return bookingType;
    }
}