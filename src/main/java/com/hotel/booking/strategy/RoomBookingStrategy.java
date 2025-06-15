package com.hotel.booking.strategy;
import com.hotel.booking.client.PaymentClient;
import com.hotel.booking.constants.BookingStatus;
import com.hotel.booking.decorator.BookingComponent;
import com.hotel.booking.decorator.DecoratorFactory;
import com.hotel.booking.dto.*;
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

import java.time.temporal.ChronoUnit;

@Component
class RoomBookingStrategy implements BookingStrategy {
    @Autowired private RoomBookingRepository repo;
    @Autowired private RoomRepository roomRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private HotelRepository hotelRepo;
    @Autowired private DecoratorFactory decoratorFactory;
    @Autowired private PaymentClient paymentClient;

    @Override @Transactional
    public BookingResponse book(BookingRequest request) {
        RoomBookingRequest r = (RoomBookingRequest) request;
        if (r.checkIn().isAfter(r.checkOut())) throw new BookingValidationException("Check-in must be before check-out.");

        User user = userRepo.findById(r.userId()).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        hotelRepo.findById(r.hotelId()).orElseThrow(() -> new ResourceNotFoundException("Hotel not found."));
        Room room = roomRepo.findByIdAndHotelId(r.roomId(), r.hotelId()).orElseThrow(() -> new ResourceNotFoundException("Room not found for this hotel."));

        if (repo.existsByRoomAndCheckInBeforeAndCheckOutAfter(room, r.checkOut(), r.checkIn())) {
            throw new BookingConflictException("Room is unavailable for these dates.");
        }

        RoomBooking booking = new RoomBooking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckIn(r.checkIn());
        booking.setCheckOut(r.checkOut());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setAppliedDecorators(r.decorators());

        BookingComponent decoratedBooking = getDecoratedBooking(booking);
        booking.setTotalPrice(decoratedBooking.getPrice());
        booking.setDescription(decoratedBooking.getDescription());

        RoomBooking saved = repo.save(booking);

        TransactionRequest transactionRequest = new TransactionRequest(saved.getId(), saved.getUser().getId(), booking.getTotalPrice(), "PAYMENT");
        String response = paymentClient.processTransaction(transactionRequest);
        System.out.println("Payment Service Response: " + response);

        return new BookingResponse(saved.getId(), saved.getUser().getId(), saved.getStatus(), saved.getTotalPrice(), saved.getDescription(), "Room booking successful.");
    }

    @Override @Transactional
    public BookingResponse modify(Long bookingId, BookingModificationRequest request) {
        RoomBookingModificationRequest r = (RoomBookingModificationRequest) request;
        if (r.checkIn().isAfter(r.checkOut())) throw new BookingValidationException("Check-in must be before check-out.");

        RoomBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (repo.existsByRoomAndCheckInBeforeAndCheckOutAfterAndIdNot(booking.getRoom(), r.checkOut(), r.checkIn(), bookingId)) {
            throw new BookingConflictException("Room is unavailable for the new dates.");
        }

        booking.setCheckIn(r.checkIn());
        booking.setCheckOut(r.checkOut());
        booking.setStatus(BookingStatus.MODIFIED);
        booking.setAppliedDecorators(r.decorators());
        RoomBooking saved = repo.save(booking);

        TransactionRequest transactionRequest = new TransactionRequest(saved.getId(), saved.getUser().getId(), booking.getTotalPrice(), "PAYMENT");
        String response = paymentClient.processTransaction(transactionRequest);
        System.out.println("Payment Service Response: " + response);

        return new BookingResponse(booking.getId(), booking.getUser().getId(), booking.getStatus(), saved.getTotalPrice(), saved.getDescription(),"Booking modified successfully.");
    }

    @Override @Transactional
    public BookingResponse cancel(Long bookingId) {
        RoomBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (booking.getStatus() == BookingStatus.CANCELLED) throw new BookingValidationException("Booking is already cancelled.");

        booking.setStatus(BookingStatus.CANCELLED);
        RoomBooking saved =repo.save(booking);

        TransactionRequest transactionRequest = new TransactionRequest(saved.getId(), saved.getUser().getId(), booking.getTotalPrice(), "REFUND");
        String response = paymentClient.processTransaction(transactionRequest);
        System.out.println("Payment Service Response: " + response);

        return new BookingResponse(booking.getId(), booking.getUser().getId(), booking.getStatus(), saved.getTotalPrice(), saved.getDescription(),"Booking cancelled successfully.");
    }

    @Override @Transactional(readOnly = true)
    public BookingDetailsResponse findById(Long bookingId) {
        RoomBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));

        Room room = booking.getRoom();
        RoomBookingDetails details = new RoomBookingDetails(room.getId(), room.getType(), booking.getCheckIn(), booking.getCheckOut());
        return new BookingDetailsResponse(booking.getId(), booking.getStatus(), booking.getUser().getId(), booking.getUser().getName(), room.getHotel().getId(), room.getHotel().getName(), details, booking.getTotalPrice(), booking.getDescription());
    }

    private BookingComponent getDecoratedBooking(RoomBooking booking) {
        final long nights = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());

        BookingComponent bookingComponent = new BookingComponent() {
            @Override public double getPrice() { return booking.getRoom().getPrice() * nights; }
            @Override public String getDescription() { return booking.getRoom().getType() + " Room for " + nights + " night(s)"; }
        };

        return decoratorFactory.applyDecorators(bookingComponent, booking.getAppliedDecorators(), nights);
    }

    @Override public String getStrategyType() { return "ROOM"; }
}