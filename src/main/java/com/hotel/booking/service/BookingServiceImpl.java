package com.hotel.booking.service;

import com.hotel.booking.dto.*;
import com.hotel.booking.entities.Room;
import com.hotel.booking.exception.BookingValidationException;
import com.hotel.booking.repository.RoomBookingRepository;
import com.hotel.booking.repository.RoomRepository;
import com.hotel.booking.strategy.BookingStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    @Autowired
    private BookingStrategyFactory strategyFactory;
    @Autowired private RoomRepository roomRepo;

    @Override @Transactional
    public BookingResponse processBooking(BookingRequest request) {
        return strategyFactory.getStrategy(request.getBookingType()).book(request);
    }

    @Override @Transactional
    public BookingResponse modifyBooking(Long bookingId, BookingModificationRequest request) {
        return strategyFactory.getStrategy(request.getBookingType()).modify(bookingId, request);
    }

    @Override @Transactional
    public BookingResponse cancelBooking(Long bookingId, String bookingType) {
        return strategyFactory.getStrategy(bookingType).cancel(bookingId);
    }

    @Override @Transactional(readOnly = true)
    public BookingDetailsResponse findBookingById(Long bookingId, String bookingType) {
        return strategyFactory.getStrategy(bookingType).findById(bookingId);
    }

    @Override @Transactional(readOnly = true)
    public List<AvailabilityResponse> findAvailableRooms(Long hotelId, LocalDate checkIn, LocalDate checkOut, int minCapacity) {
        if (checkIn.isAfter(checkOut)) { throw new BookingValidationException("Check-in date must be before check-out date."); }
        List<Room> availableRooms = roomRepo.findByHotelIdAndCapacityGreaterThanEqualAndAvailableTrue(hotelId, minCapacity);
        return availableRooms.stream()
                .map(room -> new AvailabilityResponse(room.getId(), room.getType(), room.getPrice(), room.getCapacity()))
                .collect(Collectors.toList());
    }
}