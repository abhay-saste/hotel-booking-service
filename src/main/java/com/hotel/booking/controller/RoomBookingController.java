package com.hotel.booking.controller;

import com.hotel.booking.dto.*;
import com.hotel.booking.exception.BookingValidationException;
import com.hotel.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bookings/rooms")
public class RoomBookingController {
    @Autowired
    private BookingService bookingService;

    @GetMapping("/availability")
    public ResponseEntity<List<AvailabilityResponse>> checkRoomAvailability(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int minCapacity) {
        List<AvailabilityResponse> availableRooms = bookingService.findAvailableRooms(hotelId, checkIn, checkOut, minCapacity);
        return ResponseEntity.ok(availableRooms);
    }

    @PostMapping()
    public ResponseEntity<BookingResponse> bookRoom(@Valid @RequestBody RoomBookingRequest request) {
        BookingResponse response = bookingService.processBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}/{bookingId}")
    public ResponseEntity<BookingDetailsResponse> getRoomBooking(@PathVariable Long hotelId, @PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.findBookingById(bookingId, "ROOM"));
    }

    @PutMapping("/{hotelId}/{bookingId}")
    public ResponseEntity<BookingResponse> modifyRoomBooking(@PathVariable Long hotelId, @PathVariable Long bookingId, @Valid @RequestBody RoomBookingModificationRequest request) {
        return ResponseEntity.ok(bookingService.modifyBooking(bookingId, request));
    }

    @DeleteMapping("/{hotelId}/{bookingId}")
    public ResponseEntity<BookingResponse> cancelRoomBooking(@PathVariable Long hotelId, @PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, "ROOM"));
    }
}
