package com.hotel.booking.dto;

public record AvailabilityResponse(Long roomId, String type, double price, int capacity) {}
