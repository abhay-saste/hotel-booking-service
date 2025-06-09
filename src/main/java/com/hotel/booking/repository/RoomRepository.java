package com.hotel.booking.repository;

import com.hotel.booking.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByIdAndHotelId(Long id, Long hotelId);

    List<Room> findByHotelIdAndCapacityGreaterThanEqualAndAvailableTrue(Long hotelId, int minCapacity);
}