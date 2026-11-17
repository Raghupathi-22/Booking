package com.hyderabad.booking.dto;

import com.hyderabad.booking.model.Booking;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingDto {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long partnerId;
    private String businessName;
    private Long serviceId;
    private String serviceName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
    private String bookingType;
    private String recurrenceRule;
    private BigDecimal amount;
    private BigDecimal commissionAmount;
    private String paymentStatus;
    private LocalDateTime createdAt;

    public BookingDto() {}

    public BookingDto(Booking booking) {
        this.id = booking.getId();
        this.customerId = booking.getCustomer() != null ? booking.getCustomer().getId() : null;
        this.customerName = booking.getCustomer() != null ? booking.getCustomer().getName() : null;
        this.partnerId = booking.getPartner() != null ? booking.getPartner().getId() : null;
        this.businessName = booking.getPartner() != null ? booking.getPartner().getBusinessName() : null;
        this.serviceId = booking.getService() != null ? booking.getService().getId() : null;
        this.serviceName = booking.getService() != null ? booking.getService().getName() : null;
        this.startAt = booking.getStartAt();
        this.endAt = booking.getEndAt();
        this.status = booking.getStatus();
        this.bookingType = booking.getBookingType();
        this.recurrenceRule = booking.getRecurrenceRule();
        this.amount = booking.getAmount();
        this.commissionAmount = booking.getCommissionAmount();
        this.paymentStatus = booking.getPaymentStatus();
        this.createdAt = booking.getCreatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public String getRecurrenceRule() { return recurrenceRule; }
    public void setRecurrenceRule(String recurrenceRule) { this.recurrenceRule = recurrenceRule; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
