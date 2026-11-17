package com.hyderabad.booking.dto;

import com.hyderabad.booking.model.TimeOff;
import java.time.LocalDateTime;

public class TimeOffDto {
    private Long id;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public TimeOffDto() {}

    public TimeOffDto(TimeOff timeOff) {
        this.id = timeOff.getId();
        this.startAt = timeOff.getStartAt();
        this.endAt = timeOff.getEndAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
}
