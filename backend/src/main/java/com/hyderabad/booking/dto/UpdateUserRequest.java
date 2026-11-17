package com.hyderabad.booking.dto;

public class UpdateUserRequest {
    private String name;
    private String fcmToken;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String name, String fcmToken) {
        this.name = name;
        this.fcmToken = fcmToken;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
}
