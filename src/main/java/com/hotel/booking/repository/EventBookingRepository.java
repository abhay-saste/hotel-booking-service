package com.hotel.booking.repository;

import com.hotel.booking.entities.Event;
import com.hotel.booking.entities.EventBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface EventBookingRepository extends JpaRepository<EventBooking, Long> {
    boolean existsByEventAndEventDate(Event event, LocalDate date);
    boolean existsByEventAndEventDateAndIdNot(Event event, LocalDate date, Long bookingId);
}