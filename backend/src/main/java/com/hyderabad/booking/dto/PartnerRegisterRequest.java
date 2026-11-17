package com.hyderabad.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public class PartnerRegisterRequest {

    @NotBlank(message = "Business type is required")
    @Pattern(regexp = "SALON|TUTOR", message = "Type must be SALON or TUTOR")
    private String type;

    @NotBlank(message = "Business name is required")
    private String businessName;

    private String description;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;

    @NotBlank(message = "Area is required")
    private String area;

    public PartnerRegisterRequest() {}

    public PartnerRegisterRequest(String type, String businessName, String description, String address, BigDecimal lat, BigDecimal lng, String area) {
        this.type = type;
        this.businessName = businessName;
        this.description = description;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.area = area;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }

    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
}
