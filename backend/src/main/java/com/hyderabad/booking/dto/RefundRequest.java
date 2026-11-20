package com.hyderabad.booking.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class RefundRequest {

    @NotNull
    private Long bookingId;

    @NotNull
    private BigDecimal refundAmount;

    public RefundRequest() {}

    public RefundRequest(Long bookingId, BigDecimal refundAmount) {
        this.bookingId = bookingId;
        this.refundAmount = refundAmount;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
}
