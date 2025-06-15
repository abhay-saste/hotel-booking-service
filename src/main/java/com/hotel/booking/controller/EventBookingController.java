package com.hotel.booking.controller;

import com.hotel.booking.dto.BookingDetailsResponse;
import com.hotel.booking.dto.BookingResponse;
import com.hotel.booking.dto.EventBookingModificationRequest;
import com.hotel.booking.dto.EventBookingRequest;
import com.hotel.booking.exception.BookingValidationException;
import com.hotel.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings/events")
public class EventBookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping()
    public ResponseEntity<BookingResponse> bookEvent(@Valid @RequestBody EventBookingRequest request) {
         BookingResponse response = bookingService.processBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}/{bookingId}")
    public ResponseEntity<BookingDetailsResponse> getEventBooking(@PathVariable Long hotelId, @PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.findBookingById(bookingId, "EVENT"));
    }

    @PutMapping("/bookings/events/{bookingId}")
    public ResponseEntity<BookingResponse> modifyEventBooking(@PathVariable Long hotelId, @PathVariable Long bookingId, @Valid @RequestBody EventBookingModificationRequest request) {
        return ResponseEntity.ok(bookingService.modifyBooking(bookingId, request));
    }

    @DeleteMapping("/bookings/events/{bookingId}")
    public ResponseEntity<BookingResponse> cancelEventBooking(@PathVariable Long hotelId, @PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, "EVENT"));
    }
}
