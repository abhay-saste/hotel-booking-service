package com.hotel.booking.strategy;

import com.hotel.booking.client.PaymentClient;
import com.hotel.booking.constants.BookingStatus;
import com.hotel.booking.decorator.BookingComponent;
import com.hotel.booking.decorator.DecoratorFactory;
import com.hotel.booking.dto.*;
import com.hotel.booking.entities.*;
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

import java.time.temporal.ChronoUnit;

@Component
class EventBookingStrategy implements BookingStrategy {
    @Autowired
    private EventBookingRepository repo;
    @Autowired
    private EventRepository eventRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private HotelRepository hotelRepo;
    @Autowired
    private DecoratorFactory decoratorFactory;
    @Autowired private PaymentClient paymentClient;

    @Override
    @Transactional
    public BookingResponse book(BookingRequest request) {
        EventBookingRequest r = (EventBookingRequest) request;
        User user = userRepo.findById(r.userId()).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        hotelRepo.findById(r.hotelId()).orElseThrow(() -> new ResourceNotFoundException("Hotel not found."));
        Event event = eventRepo.findByNameAndHotelId(r.eventName(), r.hotelId()).orElseThrow(() -> new ResourceNotFoundException("Event not found for this hotel."));

        if (repo.existsByEventAndEventDate(event, r.eventDate())) {
            throw new BookingConflictException("Event is unavailable for this date.");
        }

        EventBooking booking = new EventBooking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setEventDate(r.eventDate());
        booking.setAttendees(r.attendees());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setAppliedDecorators(r.decorators());

        BookingComponent decoratedBooking = getDecoratedBooking(booking);
        booking.setTotalPrice(decoratedBooking.getPrice());
        booking.setDescription(decoratedBooking.getDescription());

        EventBooking saved = repo.save(booking);

        TransactionRequest transactionRequest = new TransactionRequest(saved.getId(), saved.getUser().getId(), decoratedBooking.getPrice(), "PAYMENT");
        String response = paymentClient.processTransaction(transactionRequest);
        System.out.println("Payment Service Response: " + response);

        return new BookingResponse(saved.getId(), saved.getUser().getId(), saved.getStatus(), saved.getTotalPrice(), saved.getDescription(), "Event booking successful.");
    }

    @Override
    @Transactional
    public BookingResponse modify(Long bookingId, BookingModificationRequest request) {
        EventBookingModificationRequest r = (EventBookingModificationRequest) request;
        EventBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));

        if (!booking.getEventDate().isEqual(r.eventDate()) && repo.existsByEventAndEventDateAndIdNot(booking.getEvent(), r.eventDate(), bookingId)) {
            throw new BookingConflictException("Event is unavailable for the new date.");
        }

        booking.setEventDate(r.eventDate());
        booking.setAttendees(r.attendees());
        booking.setStatus(BookingStatus.MODIFIED);
        booking.setAppliedDecorators(r.decorators());
        EventBooking saved = repo.save(booking);

        TransactionRequest transactionRequest = new TransactionRequest(saved.getId(), saved.getUser().getId(), booking.getTotalPrice(), "PAYMENT");
        String response = paymentClient.processTransaction(transactionRequest);
        System.out.println("Payment Service Response: " + response);

        return new BookingResponse(booking.getId(), booking.getUser().getId(), booking.getStatus(), saved.getTotalPrice(), saved.getDescription(),"Event booking modified successfully.");
    }

    @Override
    @Transactional
    public BookingResponse cancel(Long bookingId) {
        EventBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new BookingValidationException("Booking is already cancelled.");

        booking.setStatus(BookingStatus.CANCELLED);
        EventBooking saved = repo.save(booking);

        TransactionRequest transactionRequest = new TransactionRequest(saved.getId(), saved.getUser().getId(), booking.getTotalPrice(), "REFUND");
        String response = paymentClient.processTransaction(transactionRequest);
        System.out.println("Payment Service Response: " + response);

        return new BookingResponse(booking.getId(), booking.getUser().getId(), booking.getStatus(), saved.getTotalPrice(), saved.getDescription(),"Booking cancelled successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailsResponse findById(Long bookingId) {
        EventBooking booking = repo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        Event event = booking.getEvent();
        EventBookingDetails details = new EventBookingDetails(event.getId(), event.getName(), booking.getEventDate(), booking.getAttendees());
        return new BookingDetailsResponse(booking.getId(), booking.getStatus(), booking.getUser().getId(), booking.getUser().getName(), event.getHotel().getId(), event.getHotel().getName(), details, booking.getTotalPrice(), booking.getDescription());
    }

    private BookingComponent getDecoratedBooking(EventBooking booking) {
        Event event = booking.getEvent();
        BookingComponent bookingComponent = new BookingComponent() {
            @Override
            public double getPrice() {
                int capacity = event.getCapacity();
                int attendees = booking.getAttendees();
                if(attendees > capacity) {
                    return event.getBasePrice() + 10000;
                }
                return event.getBasePrice();
            }

            @Override
            public String getDescription() {
                return event.getName() + " for " + booking.getAttendees() + " attendees";
            }
        };

        return decoratorFactory.applyDecorators(bookingComponent, booking.getAppliedDecorators(), 0);
    }

    @Override
    public String getStrategyType() {
        return "EVENT";
    }
}