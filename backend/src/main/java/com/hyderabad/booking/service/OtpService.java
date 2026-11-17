package com.hyderabad.booking.service;

import com.hyderabad.booking.exception.BadRequestException;
import com.hyderabad.booking.exception.RateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final long OTP_VALIDITY_SECONDS = 300; // 5 minutes
    private static final long RATE_LIMIT_SECONDS = 60; // 1 minute between requests

    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastRequestTime = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateAndSendOtp(String phone) {
        Instant now = Instant.now();
        Instant lastReq = lastRequestTime.get(phone);
        if (lastReq != null && lastReq.plusSeconds(RATE_LIMIT_SECONDS).isAfter(now)) {
            throw new RateLimitException("Please wait before requesting another OTP");
        }

        // Generate 6 digit OTP
        String otp = String.format("%06d", random.nextInt(900000) + 100000);
        Instant expiry = now.plusSeconds(OTP_VALIDITY_SECONDS);

        otpStorage.put(phone, new OtpEntry(otp, expiry));
        lastRequestTime.put(phone, now);

        log.info("========== OTP FOR [{}]: {} ==========", phone, otp);
        return otp;
    }

    public boolean verifyOtp(String phone, String otpInput) {
        OtpEntry entry = otpStorage.get(phone);
        if (entry == null) {
            throw new BadRequestException("OTP not requested or expired");
        }
        if (Instant.now().isAfter(entry.expiry)) {
            otpStorage.remove(phone);
            throw new BadRequestException("OTP has expired");
        }
        if (!entry.otp.equals(otpInput)) {
            throw new BadRequestException("Invalid OTP");
        }

        // OTP verified successfully, remove it
        otpStorage.remove(phone);
        return true;
    }

    private static class OtpEntry {
        String otp;
        Instant expiry;

        OtpEntry(String otp, Instant expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}
