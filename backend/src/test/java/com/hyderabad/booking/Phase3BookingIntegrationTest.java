package com.hyderabad.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyderabad.booking.dto.*;
import com.hyderabad.booking.model.Partner;
import com.hyderabad.booking.repository.PartnerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class Phase3BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartnerRepository partnerRepository;

    @Test
    void testServicesWorkingHoursSlotsAndBookingLifecycle() throws Exception {
        String partnerPhone = "+919888877777";
        String customerPhone = "+919888866666";

        // 1. Authenticate Partner
        String partnerToken = getAuthToken(partnerPhone, "Salon Owner", "PARTNER");

        // 2. Register Partner (Salon)
        PartnerRegisterRequest partnerReq = new PartnerRegisterRequest(
                "SALON", "Style Lounge", "Hair and Spa", "Hitech City",
                new BigDecimal("17.4486"), new BigDecimal("78.3808"), "Hitech City"
        );

        MvcResult pRegRes = mockMvc.perform(post("/api/v1/partner/register")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partnerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        PartnerDto partnerDto = objectMapper.readValue(pRegRes.getResponse().getContentAsString(), PartnerDto.class);
        Long partnerId = partnerDto.getId();

        // Approve partner directly in DB for testing
        Partner partner = partnerRepository.findById(partnerId).orElseThrow();
        partner.setStatus("APPROVED");
        partnerRepository.save(partner);

        // 3. Create Services (Salon + Tuition for Demo test)
        ServiceRequest salonServiceReq = new ServiceRequest();
        salonServiceReq.setCategory("SALON");
        salonServiceReq.setName("Hair Styling");
        salonServiceReq.setDescription("Premium Hair Styling");
        salonServiceReq.setPrice(new BigDecimal("1000.00"));
        salonServiceReq.setDurationMinutes(60);
        salonServiceReq.setMode("AT_SHOP");
        salonServiceReq.setActive(true);

        MvcResult sRes = mockMvc.perform(post("/api/v1/partner/services")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salonServiceReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Hair Styling"))
                .andReturn();

        ServiceDto salonServiceDto = objectMapper.readValue(sRes.getResponse().getContentAsString(), ServiceDto.class);

        // 4. Set Working Hours
        WorkingHoursRequest whMon = new WorkingHoursRequest("MONDAY", LocalTime.of(9, 0), LocalTime.of(17, 0));
        mockMvc.perform(put("/api/v1/partner/working-hours")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(whMon))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));

        // 5. Query Slots for next Monday
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        MvcResult slotsRes = mockMvc.perform(get("/api/v1/partners/" + partnerId + "/slots")
                        .param("date", nextMonday.toString())
                        .param("serviceId", salonServiceDto.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        List<SlotDto> slots = objectMapper.readValue(
                slotsRes.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, SlotDto.class)
        );
        assertFalse(slots.isEmpty(), "Available slots should be generated for working hours");

        // 6. Authenticate Customer & Book First Slot
        String customerToken = getAuthToken(customerPhone, "Jane Customer", "CUSTOMER");
        LocalDateTime bookedSlotStart = slots.get(0).getStartAt();

        BookingRequest bookingReq = new BookingRequest();
        bookingReq.setPartnerId(partnerId);
        bookingReq.setServiceId(salonServiceDto.getId());
        bookingReq.setStartAt(bookedSlotStart);
        bookingReq.setBookingType("SINGLE");

        MvcResult bRes = mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].amount").value(1000.00))
                .andExpect(jsonPath("$[0].commissionAmount").value(100.00)) // 10% of 1000
                .andReturn();

        List<BookingDto> bookingsCreated = objectMapper.readValue(
                bRes.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, BookingDto.class)
        );
        Long bookingId = bookingsCreated.get(0).getId();

        // 7. Verify booked slot is removed from available slots
        MvcResult updatedSlotsRes = mockMvc.perform(get("/api/v1/partners/" + partnerId + "/slots")
                        .param("date", nextMonday.toString())
                        .param("serviceId", salonServiceDto.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        List<SlotDto> updatedSlots = objectMapper.readValue(
                updatedSlotsRes.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, SlotDto.class)
        );
        assertEquals(slots.size() - 1, updatedSlots.size(), "Booked slot should be excluded from available slots");

        // 8. Cancel Booking
        mockMvc.perform(post("/api/v1/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
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
