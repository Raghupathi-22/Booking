package com.hyderabad.booking.dto;

import com.hyderabad.booking.model.Payout;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayoutDto {

    private Long id;
    private Long partnerId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    public PayoutDto() {}

    public PayoutDto(Payout payout) {
        this.id = payout.getId();
        this.partnerId = payout.getPartner() != null ? payout.getPartner().getId() : null;
        this.amount = payout.getAmount();
        this.status = payout.getStatus();
        this.periodStart = payout.getPeriodStart();
        this.periodEnd = payout.getPeriodEnd();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }

    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
}
