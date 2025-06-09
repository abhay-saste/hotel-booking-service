package com.hotel.booking.repository;

import com.hotel.booking.entities.Room;
import com.hotel.booking.entities.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {

    boolean existsByRoomAndCheckInBeforeAndCheckOutAfter(Room room, LocalDate checkOut, LocalDate checkIn);

    boolean existsByRoomAndCheckInBeforeAndCheckOutAfterAndIdNot(Room room, LocalDate checkOut, LocalDate checkIn, Long bookingId);

}
