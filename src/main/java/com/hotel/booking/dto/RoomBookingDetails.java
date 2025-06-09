package com.hotel.booking.dto;

import java.time.LocalDate;

public record RoomBookingDetails(Long roomId, String roomType, LocalDate checkIn, LocalDate checkOut) {}
