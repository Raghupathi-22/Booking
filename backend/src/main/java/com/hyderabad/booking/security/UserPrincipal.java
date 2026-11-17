package com.hyderabad.booking.security;

public class UserPrincipal {
    private final Long id;
    private final String phone;
    private final String role;

    public UserPrincipal(Long id, String phone, String role) {
        this.id = id;
        this.phone = phone;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
}
