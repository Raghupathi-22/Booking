package com.hyderabad.booking.dto;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull
    private Long bookingId;

    public CreateOrderRequest() {}

    public CreateOrderRequest(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
}
