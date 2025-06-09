package com.hotel.booking.strategy;

import com.hotel.booking.constants.BookingStatus;
import com.hotel.booking.dto.*;
import com.hotel.booking.entities.Event;
import com.hotel.booking.entities.EventBooking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.exception.BookingConflictException;
import com.hotel.booking.exception.BookingValidationException;
import com.hotel.booking.exception.ResourceNotFoundException;
import com.hotel.booking.repository.EventBookingRepository;
import com.hotel.booking.repository.EventRepository;
import com.hotel.booking.repository.HotelRepository;
import com.hotel.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class EventBookingStrategy implements BookingStrategy {
    @Autowired
    private EventBookingRepository repo;
    @Autowired private EventRepository eventRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private HotelRepository hotelRepo;

    @Override
    public BookingResponse book(BookingRequest request) { /* ... same as before ... */
        EventBookingRequest eventRequest = (EventBookingRequest) request;
        hotelRepo.findById(eventRequest.hotelId()).orElseThrow(() -> new ResourceNotFoundException("Hotel not found."));
        User user = userRepo.findById(eventRequest.userId()).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        Event event = eventRepo.findByIdAndHotelId(eventRequest.eventId(), eventRequest.hotelId()).orElseThrow(() -> new ResourceNotFoundException("Event not found for this hotel."));
        if (repo.existsByEventAndEventDate(event, eventRequest.eventDate())) { throw new BookingConflictException("Event is unavailable for this date."); }
        EventBooking booking = new EventBooking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setEventDate(eventRequest.eventDate());
        booking.setAttendees(eventRequest.attendees());
        booking.setStatus(BookingStatus.CONFIRMED);
        EventBooking saved = repo.save(booking);
        return new BookingResponse(saved.getId(), saved.getUser().getId(), saved.getStatus(), "Event booking successful.");
    }

    @Override @Transactional
    public BookingResponse modify(Long bookingId, BookingModificationRequest request) {
        EventBookingModificationRequest modRequest = (EventBookingModificationRequest) request;
        EventBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));

        // If date is changing, check availability
        if (!booking.getEventDate().isEqual(modRequest.eventDate())) {
            if (repo.existsByEventAndEventDateAndIdNot(booking.getEvent(), modRequest.eventDate(), bookingId)) {
                throw new BookingConflictException("Event is unavailable for the new date.");
            }
            booking.setEventDate(modRequest.eventDate());
        }

        booking.setAttendees(modRequest.attendees());
        booking.setStatus(BookingStatus.MODIFIED);
        repo.save(booking);
        return new BookingResponse(booking.getId(), booking.getUser().getId(), booking.getStatus(), "Event booking modified successfully.");
    }

    @Override @Transactional
    public BookingResponse cancel(Long bookingId) {
        EventBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (booking.getStatus() == BookingStatus.CANCELLED) { throw new BookingValidationException("Booking is already cancelled."); }
        booking.setStatus(BookingStatus.CANCELLED);
        repo.save(booking);
        return new BookingResponse(booking.getId(), booking.getUser().getId(), booking.getStatus(), "Booking cancelled successfully.");
    }

    @Override @Transactional(readOnly = true)
    public BookingDetailsResponse findById(Long bookingId) {
        EventBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        Event event = booking.getEvent();
        Hotel hotel = event.getHotel();
        User user = booking.getUser();
        EventBookingDetails details = new EventBookingDetails(event.getId(), event.getName(), booking.getEventDate(), booking.getAttendees());
        return new BookingDetailsResponse(booking.getId(), booking.getStatus(), user.getId(), user.getName(), hotel.getId(), hotel.getName(), details);
    }

    @Override public String getStrategyType() { return "EVENT"; }
}