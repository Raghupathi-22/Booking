package com.hyderabad.booking.dto;

import com.hyderabad.booking.model.Partner;
import java.math.BigDecimal;
import java.util.List;

public class PartnerDetailDto {
    private Long id;
    private String businessName;
    private String type;
    private String description;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    private String area;
    private String status;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private List<ServiceDto> services;
    private List<WorkingHoursDto> workingHours;
    private List<TimeOffDto> timeOff;
    private List<ReviewDto> reviews;
    private List<PartnerDocumentDto> documents;

    public PartnerDetailDto() {}

    public PartnerDetailDto(Partner partner, List<ServiceDto> services, List<WorkingHoursDto> workingHours, List<TimeOffDto> timeOff, List<ReviewDto> reviews) {
        this(partner, services, workingHours, timeOff, reviews, List.of());
    }

    public PartnerDetailDto(Partner partner, List<ServiceDto> services, List<WorkingHoursDto> workingHours, List<TimeOffDto> timeOff, List<ReviewDto> reviews, List<PartnerDocumentDto> documents) {
        this.id = partner.getId();
        this.businessName = partner.getBusinessName();
        this.type = partner.getType();
        this.description = partner.getDescription();
        this.address = partner.getAddress();
        this.lat = partner.getLat();
        this.lng = partner.getLng();
        this.area = partner.getArea();
        this.status = partner.getStatus();
        this.ratingAvg = partner.getRatingAvg();
        this.ratingCount = partner.getRatingCount();
        this.services = services;
        this.workingHours = workingHours;
        this.timeOff = timeOff;
        this.reviews = reviews;
        this.documents = documents;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

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

    public BigDecimal getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }

    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }

    public List<ServiceDto> getServices() { return services; }
    public void setServices(List<ServiceDto> services) { this.services = services; }

    public List<WorkingHoursDto> getWorkingHours() { return workingHours; }
    public void setWorkingHours(List<WorkingHoursDto> workingHours) { this.workingHours = workingHours; }

    public List<TimeOffDto> getTimeOff() { return timeOff; }
    public void setTimeOff(List<TimeOffDto> timeOff) { this.timeOff = timeOff; }

    public List<ReviewDto> getReviews() { return reviews; }
    public void setReviews(List<ReviewDto> reviews) { this.reviews = reviews; }

    public List<PartnerDocumentDto> getDocuments() { return documents; }
    public void setDocuments(List<PartnerDocumentDto> documents) { this.documents = documents; }
}
