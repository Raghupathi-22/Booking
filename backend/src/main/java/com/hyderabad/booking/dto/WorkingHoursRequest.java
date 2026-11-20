package com.hyderabad.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class WorkingHoursRequest {

    @NotBlank
    private String dayOfWeek;

    @NotNull
    private LocalTime openTime;

    @NotNull
    private LocalTime closeTime;

    public WorkingHoursRequest() {}

    public WorkingHoursRequest(String dayOfWeek, LocalTime openTime, LocalTime closeTime) {
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
}
