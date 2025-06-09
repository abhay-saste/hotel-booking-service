package com.hotel.booking.dto;

import java.time.LocalDate;

public record EventBookingDetails(Long eventId, String eventName, LocalDate eventDate, int attendees) {}