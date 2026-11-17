package com.hyderabad.booking.dto;

import com.hyderabad.booking.model.WorkingHours;
import java.time.LocalTime;

public class WorkingHoursDto {
    private Long id;
    private String dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;

    public WorkingHoursDto() {}

    public WorkingHoursDto(WorkingHours wh) {
        this.id = wh.getId();
        this.dayOfWeek = wh.getDayOfWeek();
        this.openTime = wh.getOpenTime();
        this.closeTime = wh.getCloseTime();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
}
