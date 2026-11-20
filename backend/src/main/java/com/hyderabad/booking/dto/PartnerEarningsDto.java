package com.hyderabad.booking.dto;

import java.math.BigDecimal;

public class PartnerEarningsDto {

    private int totalBookings;
    private BigDecimal totalRevenue;
    private BigDecimal totalCommission;
    private BigDecimal netEarnings;
    private BigDecimal pendingPayout;

    public PartnerEarningsDto() {}

    public PartnerEarningsDto(int totalBookings, BigDecimal totalRevenue, BigDecimal totalCommission, BigDecimal netEarnings, BigDecimal pendingPayout) {
        this.totalBookings = totalBookings;
        this.totalRevenue = totalRevenue;
        this.totalCommission = totalCommission;
        this.netEarnings = netEarnings;
        this.pendingPayout = pendingPayout;
    }

    public int getTotalBookings() { return totalBookings; }
    public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getTotalCommission() { return totalCommission; }
    public void setTotalCommission(BigDecimal totalCommission) { this.totalCommission = totalCommission; }

    public BigDecimal getNetEarnings() { return netEarnings; }
    public void setNetEarnings(BigDecimal netEarnings) { this.netEarnings = netEarnings; }

    public BigDecimal getPendingPayout() { return pendingPayout; }
    public void setPendingPayout(BigDecimal pendingPayout) { this.pendingPayout = pendingPayout; }
}
