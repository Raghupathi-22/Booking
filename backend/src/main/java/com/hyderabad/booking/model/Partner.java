package com.hyderabad.booking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "partners")
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String type; // SALON, TUTOR

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(precision = 10, scale = 8)
    private BigDecimal lat;

    @Column(precision = 11, scale = 8)
    private BigDecimal lng;

    @Column(length = 100)
    private String area;

    @Column(length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, SUSPENDED

    @Column(name = "commission_percent", precision = 5, scale = 2)
    private BigDecimal commissionPercent = new BigDecimal("10.00");

    @Column(name = "rating_avg", precision = 3, scale = 2)
    private BigDecimal ratingAvg = new BigDecimal("0.00");

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    public Partner() {}

    public Partner(User user, String type, String businessName, String description, String address, BigDecimal lat, BigDecimal lng, String area) {
        this.user = user;
        this.type = type;
        this.businessName = businessName;
        this.description = description;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.area = area;
        this.status = "PENDING";
        this.commissionPercent = new BigDecimal("10.00");
        this.ratingAvg = new BigDecimal("0.00");
        this.ratingCount = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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
