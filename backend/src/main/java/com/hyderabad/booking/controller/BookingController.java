package com.hyderabad.booking.controller;

import com.hyderabad.booking.dto.BookingDto;
import com.hyderabad.booking.dto.BookingRequest;
import com.hyderabad.booking.security.UserPrincipal;
import com.hyderabad.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<List<BookingDto>> createBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BookingRequest request) {
        List<BookingDto> bookings = bookingService.createBooking(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookings);
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getUserBookings(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<BookingDto> bookings = bookingService.getUserBookings(principal.getId());
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingDto> cancelBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        BookingDto bookingDto = bookingService.cancelBooking(principal.getId(), id);
        return ResponseEntity.ok(bookingDto);
    }
}
