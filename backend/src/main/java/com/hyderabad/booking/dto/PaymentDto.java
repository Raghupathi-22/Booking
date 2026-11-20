package com.hyderabad.booking.dto;

import com.hyderabad.booking.model.Payment;
import java.math.BigDecimal;

public class PaymentDto {

    private Long id;
    private Long bookingId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String status;
    private BigDecimal amount;
    private BigDecimal refundedAmount;

    public PaymentDto() {}

    public PaymentDto(Payment payment) {
        this.id = payment.getId();
        this.bookingId = payment.getBooking() != null ? payment.getBooking().getId() : null;
        this.razorpayOrderId = payment.getRazorpayOrderId();
        this.razorpayPaymentId = payment.getRazorpayPaymentId();
        this.status = payment.getStatus();
        this.amount = payment.getAmount();
        this.refundedAmount = payment.getRefundedAmount();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }
}
