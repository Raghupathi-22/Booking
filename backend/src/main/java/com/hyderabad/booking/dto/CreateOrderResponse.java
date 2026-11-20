package com.hyderabad.booking.dto;

import java.math.BigDecimal;

public class CreateOrderResponse {

    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency = "INR";
    private String keyId;
    private Long bookingId;

    public CreateOrderResponse() {}

    public CreateOrderResponse(String razorpayOrderId, BigDecimal amount, String currency, String keyId, Long bookingId) {
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
        this.bookingId = bookingId;
    }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
}
