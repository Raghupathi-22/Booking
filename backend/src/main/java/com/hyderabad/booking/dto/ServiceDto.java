package com.hyderabad.booking.dto;

import com.hyderabad.booking.model.ServiceEntity;
import java.math.BigDecimal;

public class ServiceDto {
    private Long id;
    private Long partnerId;
    private String category;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private String mode;
    private Boolean active;

    public ServiceDto() {}

    public ServiceDto(ServiceEntity service) {
        this.id = service.getId();
        this.partnerId = service.getPartner() != null ? service.getPartner().getId() : null;
        this.category = service.getCategory();
        this.name = service.getName();
        this.description = service.getDescription();
        this.price = service.getPrice();
        this.durationMinutes = service.getDurationMinutes();
        this.mode = service.getMode();
        this.active = service.getActive();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
