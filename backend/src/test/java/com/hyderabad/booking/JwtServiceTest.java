package com.hyderabad.booking;

import com.hyderabad.booking.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        long expiration = 86400000;
        jwtService = new JwtService(secret, expiration);
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        String phone = "+919876543210";
        Long userId = 1L;
        String role = "CUSTOMER";

        String token = jwtService.generateAccessToken(phone, userId, role);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertTrue(jwtService.isAccessToken(token));
        assertFalse(jwtService.isRefreshToken(token));

        assertEquals(phone, jwtService.extractPhone(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(role, jwtService.extractRole(token));
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        String phone = "+919876543210";
        Long userId = 1L;

        String token = jwtService.generateRefreshToken(phone, userId);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertTrue(jwtService.isRefreshToken(token));
        assertFalse(jwtService.isAccessToken(token));

        assertEquals(phone, jwtService.extractPhone(token));
        assertEquals(userId, jwtService.extractUserId(token));
    }
}
