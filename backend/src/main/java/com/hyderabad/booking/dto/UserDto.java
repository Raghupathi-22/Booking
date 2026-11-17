package com.hyderabad.booking.dto;

import com.hyderabad.booking.model.User;

public class UserDto {
    private Long id;
    private String phone;
    private String name;
    private String role;
    private String fcmToken;
    private String createdAt;

    public UserDto() {}

    public UserDto(User user) {
        this.id = user.getId();
        this.phone = user.getPhone();
        this.name = user.getName();
        this.role = user.getRole();
        this.fcmToken = user.getFcmToken();
        if (user.getCreatedAt() != null) {
            this.createdAt = user.getCreatedAt().toString();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
