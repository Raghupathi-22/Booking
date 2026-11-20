package com.hyderabad.booking.dto;

import java.math.BigDecimal;

public class AdminStatsDto {

    private long totalUsers;
    private long totalPartners;
    private long pendingPartners;
    private long approvedPartners;
    private long totalBookings;
    private BigDecimal totalRevenue;
    private BigDecimal totalCommissionEarned;

    public AdminStatsDto() {}

    public AdminStatsDto(long totalUsers, long totalPartners, long pendingPartners, long approvedPartners, long totalBookings, BigDecimal totalRevenue, BigDecimal totalCommissionEarned) {
        this.totalUsers = totalUsers;
        this.totalPartners = totalPartners;
        this.pendingPartners = pendingPartners;
        this.approvedPartners = approvedPartners;
        this.totalBookings = totalBookings;
        this.totalRevenue = totalRevenue;
        this.totalCommissionEarned = totalCommissionEarned;
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalPartners() { return totalPartners; }
    public void setTotalPartners(long totalPartners) { this.totalPartners = totalPartners; }

    public long getPendingPartners() { return pendingPartners; }
    public void setPendingPartners(long pendingPartners) { this.pendingPartners = pendingPartners; }

    public long getApprovedPartners() { return approvedPartners; }
    public void setApprovedPartners(long approvedPartners) { this.approvedPartners = approvedPartners; }

    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getTotalCommissionEarned() { return totalCommissionEarned; }
    public void setTotalCommissionEarned(BigDecimal totalCommissionEarned) { this.totalCommissionEarned = totalCommissionEarned; }
}
