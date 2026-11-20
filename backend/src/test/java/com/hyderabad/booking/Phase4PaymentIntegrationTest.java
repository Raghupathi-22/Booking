package com.hyderabad.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyderabad.booking.dto.*;
import com.hyderabad.booking.model.Partner;
import com.hyderabad.booking.repository.PartnerRepository;
import com.hyderabad.booking.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class Phase4PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PaymentService paymentService;

    @Test
    void testRazorpayPaymentFlowAndPartnerEarnings() throws Exception {
        String partnerPhone = "+919666655555";
        String customerPhone = "+919666644444";

        // 1. Authenticate Partner & Register
        String partnerToken = getAuthToken(partnerPhone, "Spa Owner", "PARTNER");
        PartnerRegisterRequest partnerReq = new PartnerRegisterRequest(
                "SALON", "Bliss Spa", "Luxury Spa", "Gachibowli",
                new BigDecimal("17.4401"), new BigDecimal("78.3489"), "Gachibowli"
        );

        MvcResult pRegRes = mockMvc.perform(post("/api/v1/partner/register")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partnerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        PartnerDto partnerDto = objectMapper.readValue(pRegRes.getResponse().getContentAsString(), PartnerDto.class);
        Partner partner = partnerRepository.findById(partnerDto.getId()).orElseThrow();
        partner.setStatus("APPROVED");
        partner.setCommissionPercent(new BigDecimal("15.00")); // 15% custom commission
        partnerRepository.save(partner);

        // 2. Create Service
        ServiceRequest sReq = new ServiceRequest();
        sReq.setCategory("SALON");
        sReq.setName("Full Body Massage");
        sReq.setDescription("Relaxing Massage");
        sReq.setPrice(new BigDecimal("2000.00"));
        sReq.setDurationMinutes(60);
        sReq.setMode("AT_SHOP");
        sReq.setActive(true);

        MvcResult sRes = mockMvc.perform(post("/api/v1/partner/services")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sReq)))
                .andExpect(status().isCreated())
                .andReturn();

        ServiceDto serviceDto = objectMapper.readValue(sRes.getResponse().getContentAsString(), ServiceDto.class);

        // 3. Authenticate Customer & Create Booking
        String customerToken = getAuthToken(customerPhone, "Customer One", "CUSTOMER");
        BookingRequest bReq = new BookingRequest();
        bReq.setPartnerId(partner.getId());
        bReq.setServiceId(serviceDto.getId());
        bReq.setStartAt(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
        bReq.setBookingType("SINGLE");

        MvcResult bRes = mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bReq)))
                .andExpect(status().isCreated())
                .andReturn();

        List<BookingDto> bookings = objectMapper.readValue(
                bRes.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, BookingDto.class)
        );
        Long bookingId = bookings.get(0).getId();

        // 4. Create Razorpay Order
        CreateOrderRequest orderReq = new CreateOrderRequest(bookingId);
        MvcResult orderRes = mockMvc.perform(post("/api/v1/payments/create-order")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razorpayOrderId").exists())
                .andExpect(jsonPath("$.amount").value(2000.00))
                .andReturn();

        CreateOrderResponse orderDto = objectMapper.readValue(
                orderRes.getResponse().getContentAsString(), CreateOrderResponse.class);
        String orderId = orderDto.getRazorpayOrderId();
        String paymentId = "pay_" + System.currentTimeMillis();
        String payload = orderId + "|" + paymentId;
        String signature = paymentService.calculateHmacSha256(payload, "dummy_secret");

        // 5. Verify Razorpay Payment Signature
        VerifyPaymentRequest verifyReq = new VerifyPaymentRequest(bookingId, orderId, paymentId, signature);
        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.razorpayPaymentId").value(paymentId));

        // 6. Verify Partner Earnings Calculation (Revenue: 2000, Commission: 15% = 300, Net: 1700)
        mockMvc.perform(get("/api/v1/partner/earnings")
                        .header("Authorization", "Bearer " + partnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").value(1))
                .andExpect(jsonPath("$.totalRevenue").value(2000.00))
                .andExpect(jsonPath("$.totalCommission").value(300.00))
                .andExpect(jsonPath("$.netEarnings").value(1700.00));

        // 7. Webhook Endpoint Test
        mockMvc.perform(post("/api/v1/webhooks/razorpay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event\":\"payment.captured\"}"))
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
