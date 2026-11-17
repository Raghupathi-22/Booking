package com.hyderabad.booking;

import com.hyderabad.booking.dto.*;
import com.hyderabad.booking.model.Partner;
import com.hyderabad.booking.repository.PartnerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingConcurrencyTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PartnerRepository partnerRepository;

    @Test
    void testConcurrentBookingsPreventDoubleBooking() throws Exception {
        String partnerPhone = "+919999911111";
        String customerPhone = "+919999922222";

        // 1. Register & Verify Partner
        ResponseEntity<SendOtpResponse> pOtpRes = restTemplate.postForEntity(
                "/api/v1/auth/send-otp", new SendOtpRequest(partnerPhone), SendOtpResponse.class);
        String pDevOtp = pOtpRes.getBody().getDevOtp();

        ResponseEntity<AuthResponse> pAuthRes = restTemplate.postForEntity(
                "/api/v1/auth/verify-otp", new VerifyOtpRequest(partnerPhone, pDevOtp, "Partner Test", "CUSTOMER"), AuthResponse.class);
        String partnerToken = pAuthRes.getBody().getAccessToken();

        HttpHeaders pHeaders = new HttpHeaders();
        pHeaders.setBearerAuth(partnerToken);
        pHeaders.setContentType(MediaType.APPLICATION_JSON);

        PartnerRegisterRequest partnerReg = new PartnerRegisterRequest(
                "SALON", "Concurrency Salon", "Desc", "Address",
                new BigDecimal("17.3850"), new BigDecimal("78.4867"), "Jubilee Hills"
        );
        restTemplate.exchange("/api/v1/partner/register", HttpMethod.POST, new HttpEntity<>(partnerReg, pHeaders), PartnerDto.class);

        Partner partner = partnerRepository.findByUserId(pAuthRes.getBody().getUser().getId()).orElseThrow();
        partner.setStatus("APPROVED");
        partnerRepository.save(partner);

        // 2. Create Service
        ServiceRequest serviceReq = new ServiceRequest();
        serviceReq.setCategory("SALON");
        serviceReq.setName("Haircut");
        serviceReq.setDescription("Stylish haircut");
        serviceReq.setPrice(new BigDecimal("500.00"));
        serviceReq.setDurationMinutes(30);
        serviceReq.setMode("AT_SHOP");
        serviceReq.setActive(true);

        ResponseEntity<ServiceDto> serviceRes = restTemplate.exchange(
                "/api/v1/partner/services", HttpMethod.POST, new HttpEntity<>(serviceReq, pHeaders), ServiceDto.class
        );
        Long serviceId = serviceRes.getBody().getId();

        // 3. Register & Verify Customer
        ResponseEntity<SendOtpResponse> cOtpRes = restTemplate.postForEntity(
                "/api/v1/auth/send-otp", new SendOtpRequest(customerPhone), SendOtpResponse.class
        );
        String cDevOtp = cOtpRes.getBody().getDevOtp();

        ResponseEntity<AuthResponse> cAuthRes = restTemplate.postForEntity(
                "/api/v1/auth/verify-otp", new VerifyOtpRequest(customerPhone, cDevOtp, "Customer Test", "CUSTOMER"), AuthResponse.class);
        String customerToken = cAuthRes.getBody().getAccessToken();

        HttpHeaders cHeaders = new HttpHeaders();
        cHeaders.setBearerAuth(customerToken);
        cHeaders.setContentType(MediaType.APPLICATION_JSON);

        // 4. Fire concurrent booking requests for the exact same slot
        LocalDateTime slotTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        BookingRequest bookingReq = new BookingRequest();
        bookingReq.setPartnerId(partner.getId());
        bookingReq.setServiceId(serviceId);
        bookingReq.setStartAt(slotTime);
        bookingReq.setBookingType("SINGLE");

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<ResponseEntity<String>>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                HttpEntity<BookingRequest> entity = new HttpEntity<>(bookingReq, cHeaders);
                return restTemplate.postForEntity("/api/v1/bookings", entity, String.class);
            }));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        int successCount = 0;
        int conflictCount = 0;

        for (Future<ResponseEntity<String>> future : futures) {
            ResponseEntity<String> res = future.get();
            if (res.getStatusCode() == HttpStatus.CREATED) {
                successCount++;
            } else if (res.getStatusCode().is4xxClientError()) {
                conflictCount++;
            }
        }

        assertEquals(1, successCount, "Only one booking request should succeed for the exact same slot");
        assertEquals(threadCount - 1, conflictCount, "All other concurrent booking requests should be rejected");
    }
}
