package com.hyderabad.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyderabad.booking.dto.SendOtpRequest;
import com.hyderabad.booking.dto.VerifyOtpRequest;
import com.hyderabad.booking.dto.RefreshTokenRequest;
import com.hyderabad.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clear users if needed or run with clean db state
    }

    @Test
    void testSendAndVerifyOtpWorkflow() throws Exception {
        String phone = "+919999988888";

        // 1. Send OTP
        SendOtpRequest sendRequest = new SendOtpRequest(phone);
        MvcResult sendResult = mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.devOtp").exists())
                .andReturn();

        String responseString = sendResult.getResponse().getContentAsString();
        com.hyderabad.booking.dto.SendOtpResponse sendOtpResponse =
                objectMapper.readValue(responseString, com.hyderabad.booking.dto.SendOtpResponse.class);
        String devOtp = sendOtpResponse.getDevOtp();

        // 2. Verify OTP
        VerifyOtpRequest verifyRequest = new VerifyOtpRequest(phone, devOtp, "Test User", "CUSTOMER");
        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.phone").value(phone))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andReturn();

        String authResponseString = verifyResult.getResponse().getContentAsString();
        com.hyderabad.booking.dto.AuthResponse authResponse =
                objectMapper.readValue(authResponseString, com.hyderabad.booking.dto.AuthResponse.class);
        String refreshToken = authResponse.getRefreshToken();

        // 3. Refresh Token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
}
