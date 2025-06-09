package com.hotel.booking.strategy;
import com.hotel.booking.constants.BookingStatus;
import com.hotel.booking.dto.*;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.Room;
import com.hotel.booking.entities.RoomBooking;
import com.hotel.booking.entities.User;
import com.hotel.booking.exception.BookingConflictException;
import com.hotel.booking.exception.BookingValidationException;
import com.hotel.booking.exception.ResourceNotFoundException;
import com.hotel.booking.repository.HotelRepository;
import com.hotel.booking.repository.RoomBookingRepository;
import com.hotel.booking.repository.RoomRepository;
import com.hotel.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class RoomBookingStrategy implements BookingStrategy {
    @Autowired private RoomBookingRepository repo;
    @Autowired private RoomRepository roomRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private HotelRepository hotelRepo;

    @Override
    public BookingResponse book(BookingRequest request) {
        RoomBookingRequest roomRequest = (RoomBookingRequest) request;
        if (roomRequest.checkIn().isAfter(roomRequest.checkOut())) { throw new BookingValidationException("Check-in must be before check-out."); }
        hotelRepo.findById(roomRequest.hotelId()).orElseThrow(() -> new ResourceNotFoundException("Hotel not found."));
        User user = userRepo.findById(roomRequest.userId()).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        Room room = roomRepo.findByIdAndHotelId(roomRequest.roomId(), roomRequest.hotelId()).orElseThrow(() -> new ResourceNotFoundException("Room not found for this hotel."));
        if (repo.existsByRoomAndCheckInBeforeAndCheckOutAfter(room, roomRequest.checkOut(), roomRequest.checkIn())) { throw new BookingConflictException("Room is unavailable for these dates."); }
        RoomBooking booking = new RoomBooking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckIn(roomRequest.checkIn());
        booking.setCheckOut(roomRequest.checkOut());
        booking.setStatus(BookingStatus.CONFIRMED);
        RoomBooking saved = repo.save(booking);
        return new BookingResponse(saved.getId(), saved.getUser().getId(), saved.getStatus(), "Room booking successful.");
    }

    @Override @Transactional
    public BookingResponse modify(Long bookingId, BookingModificationRequest request) {
        RoomBookingModificationRequest modRequest = (RoomBookingModificationRequest) request;
        if (modRequest.checkIn().isAfter(modRequest.checkOut())) { throw new BookingValidationException("Check-in must be before check-out."); }

        RoomBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));

        // Check availability for the new dates, excluding the current booking itself
        if (repo.existsByRoomAndCheckInBeforeAndCheckOutAfterAndIdNot(booking.getRoom(), modRequest.checkOut(), modRequest.checkIn(), bookingId)) {
            throw new BookingConflictException("Room is unavailable for the new dates.");
        }

        booking.setCheckIn(modRequest.checkIn());
        booking.setCheckOut(modRequest.checkOut());
        booking.setStatus(BookingStatus.MODIFIED);
        repo.save(booking);
        return new BookingResponse(booking.getId(), booking.getUser().getId(), booking.getStatus(), "Booking modified successfully.");
    }

    @Override @Transactional
    public BookingResponse cancel(Long bookingId) {
        RoomBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (booking.getStatus() == BookingStatus.CANCELLED) { throw new BookingValidationException("Booking is already cancelled."); }
        booking.setStatus(BookingStatus.CANCELLED);
        repo.save(booking);
        return new BookingResponse(booking.getId(), booking.getUser().getId(), booking.getStatus(), "Booking cancelled successfully.");
    }

    @Override @Transactional(readOnly = true)
    public BookingDetailsResponse findById(Long bookingId) {
        RoomBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        Room room = booking.getRoom();
        Hotel hotel = room.getHotel();
        User user = booking.getUser();
        RoomBookingDetails details = new RoomBookingDetails(room.getId(), room.getType(), booking.getCheckIn(), booking.getCheckOut());
        return new BookingDetailsResponse(booking.getId(), booking.getStatus(), user.getId(), user.getName(), hotel.getId(), hotel.getName(), details);
    }

    @Override public String getStrategyType() { return "ROOM"; }
}