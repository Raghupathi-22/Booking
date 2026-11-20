package com.hyderabad.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyderabad.booking.dto.*;
import com.hyderabad.booking.model.Partner;
import com.hyderabad.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class Phase5AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PartnerDocumentRepository partnerDocumentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    @Autowired
    private TimeOffRepository timeOffRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        reviewRepository.deleteAll();
        partnerDocumentRepository.deleteAll();
        serviceRepository.deleteAll();
        workingHoursRepository.deleteAll();
        timeOffRepository.deleteAll();
        partnerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testAdminPortalAndApiLifecycle() throws Exception {
        String partnerPhone = "+919555544444";

        // 1. Register Partner (Pending approval)
        String partnerToken = getAuthToken(partnerPhone, "Pending Partner", "PARTNER");
        PartnerRegisterRequest partnerReq = new PartnerRegisterRequest(
                "TUTOR", "Maths Academy", "Expert Maths Tuition", "Ameerpet",
                new BigDecimal("17.4375"), new BigDecimal("78.4482"), "Ameerpet"
        );

        MvcResult pRegRes = mockMvc.perform(post("/api/v1/partner/register")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partnerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        PartnerDto partnerDto = objectMapper.readValue(pRegRes.getResponse().getContentAsString(), PartnerDto.class);
        Long partnerId = partnerDto.getId();

        // 2. Admin GET Stats
        mockMvc.perform(get("/api/v1/admin/stats")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingPartners").value(1));

        // 3. Admin GET Pending Partners
        mockMvc.perform(get("/api/v1/admin/partners")
                        .param("status", "PENDING")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(partnerId))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        // 4. Admin Approve Partner
        mockMvc.perform(post("/api/v1/admin/partners/" + partnerId + "/approve")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // Verify partner status updated to APPROVED
        Partner approvedPartner = partnerRepository.findById(partnerId).orElseThrow();
        assertEquals("APPROVED", approvedPartner.getStatus());

        // 5. Admin Suspend Partner
        mockMvc.perform(post("/api/v1/admin/partners/" + partnerId + "/suspend")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        // Re-approve partner for booking
        mockMvc.perform(post("/api/v1/admin/partners/" + partnerId + "/approve")
                        .with(httpBasic("admin", "admin123")));

        // 6. Admin GET Bookings & Payments APIs
        mockMvc.perform(get("/api/v1/admin/bookings")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/payments")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }

    private String getAuthToken(String phone, String name, String role) throws Exception {
        SendOtpRequest sendRequest = new SendOtpRequest(phone);
        MvcResult sendResult = mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andReturn();

        SendOtpResponse sendOtpResponse = objectMapper.readValue(
                sendResult.getResponse().getContentAsString(), SendOtpResponse.class);

        VerifyOtpRequest verifyRequest = new VerifyOtpRequest(phone, sendOtpResponse.getDevOtp(), name, role);
        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                verifyResult.getResponse().getContentAsString(), AuthResponse.class);
        return authResponse.getAccessToken();
    }
}
