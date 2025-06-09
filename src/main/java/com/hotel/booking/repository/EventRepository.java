package com.hotel.booking.repository;

import com.hotel.booking.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByIdAndHotelId(Long id, Long hotelId);
}