package com.hyderabad.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyderabad.booking.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PartnerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPartnerRegistrationAndDocumentUpload() throws Exception {
        String phone = "+919777766666";

        // 1. Send OTP
        SendOtpRequest sendRequest = new SendOtpRequest(phone);
        MvcResult sendResult = mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andReturn();

        SendOtpResponse sendOtpResponse = objectMapper.readValue(
                sendResult.getResponse().getContentAsString(), SendOtpResponse.class);
        String devOtp = sendOtpResponse.getDevOtp();

        // 2. Verify OTP to get access token
        VerifyOtpRequest verifyRequest = new VerifyOtpRequest(phone, devOtp, "Partner User", "CUSTOMER");
        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                verifyResult.getResponse().getContentAsString(), AuthResponse.class);
        String accessToken = authResponse.getAccessToken();

        // 3. Register Partner
        PartnerRegisterRequest partnerRequest = new PartnerRegisterRequest(
                "SALON",
                "Glow Unisex Salon",
                "Best salon in Hyderabad",
                "Banjara Hills, Hyderabad",
                new BigDecimal("17.3850"),
                new BigDecimal("78.4867"),
                "Banjara Hills"
        );

        mockMvc.perform(post("/api/v1/partner/register")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partnerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.businessName").value("Glow Unisex Salon"))
                .andExpect(jsonPath("$.type").value("SALON"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // 4. Upload Document
        PartnerDocumentRequest docRequest = new PartnerDocumentRequest("GST_CERTIFICATE", "https://example.com/gst.pdf");
        mockMvc.perform(post("/api/v1/partner/documents")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(docRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.docType").value("GST_CERTIFICATE"))
                .andExpect(jsonPath("$.url").value("https://example.com/gst.pdf"));

        // 5. Verify partner profile GET /api/v1/partner/me
        mockMvc.perform(get("/api/v1/partner/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Glow Unisex Salon"));

        // 6. Verify user profile role updated to PARTNER via GET /api/v1/me
        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PARTNER"));
    }
}
