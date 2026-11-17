package com.hyderabad.booking.dto;

import com.hyderabad.booking.model.Partner;
import java.math.BigDecimal;

public class PartnerDto {
    private Long id;
    private Long userId;
    private String type;
    private String businessName;
    private String description;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    private String area;
    private String status;
    private BigDecimal commissionPercent;
    private BigDecimal ratingAvg;
    private Integer ratingCount;

    public PartnerDto() {}

    public PartnerDto(Partner partner) {
        this.id = partner.getId();
        if (partner.getUser() != null) {
            this.userId = partner.getUser().getId();
        }
        this.type = partner.getType();
        this.businessName = partner.getBusinessName();
        this.description = partner.getDescription();
        this.address = partner.getAddress();
        this.lat = partner.getLat();
        this.lng = partner.getLng();
        this.area = partner.getArea();
        this.status = partner.getStatus();
        this.commissionPercent = partner.getCommissionPercent();
        this.ratingAvg = partner.getRatingAvg();
        this.ratingCount = partner.getRatingCount();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getCommissionPercent() { return commissionPercent; }
    public void setCommissionPercent(BigDecimal commissionPercent) { this.commissionPercent = commissionPercent; }

    public BigDecimal getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }

    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
}
