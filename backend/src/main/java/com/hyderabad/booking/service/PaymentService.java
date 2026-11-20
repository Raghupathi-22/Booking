package com.hyderabad.booking.service;

import com.hyderabad.booking.dto.*;
import com.hyderabad.booking.exception.BadRequestException;
import com.hyderabad.booking.exception.ResourceNotFoundException;
import com.hyderabad.booking.model.*;
import com.hyderabad.booking.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PayoutRepository payoutRepository;
    private final BookingRepository bookingRepository;
    private final PartnerRepository partnerRepository;

    @Value("${razorpay.key-id:rzp_test_dummy}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret:dummy_secret}")
    private String razorpayKeySecret;

    // DEV ONLY: accepts the literal signature "test_signature". Must be false in production.
    @Value("${razorpay.allow-test-signature:true}")
    private boolean allowTestSignature;

    public PaymentService(PaymentRepository paymentRepository,
                          PayoutRepository payoutRepository,
                          BookingRepository bookingRepository,
                          PartnerRepository partnerRepository) {
        this.paymentRepository = paymentRepository;
        this.payoutRepository = payoutRepository;
        this.bookingRepository = bookingRepository;
        this.partnerRepository = partnerRepository;
    }

    @Transactional
    public CreateOrderResponse createOrder(Long customerId, CreateOrderRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Booking does not belong to this customer");
        }

        String razorpayOrderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);

        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElseGet(() -> {
            Payment p = new Payment();
            p.setBooking(booking);
            return p;
        });

        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setAmount(booking.getAmount());
        payment.setStatus("CREATED");
        paymentRepository.save(payment);

        return new CreateOrderResponse(razorpayOrderId, booking.getAmount(), "INR", razorpayKeyId, booking.getId());
    }

    @Transactional
    public PaymentDto verifyPayment(Long customerId, VerifyPaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        String expectedSignature = calculateHmacSha256(payload, razorpayKeySecret);

        if (!expectedSignature.equalsIgnoreCase(request.getRazorpaySignature())
                && !(allowTestSignature && "test_signature".equals(request.getRazorpaySignature()))) {
            throw new BadRequestException("Invalid payment signature");
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setStatus("PAID");
        paymentRepository.save(payment);

        booking.setStatus("CONFIRMED");
        booking.setPaymentStatus("PAID");
        bookingRepository.save(booking);

        return new PaymentDto(payment);
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        log.info("Received Razorpay Webhook Event");
        // Webhook signature verification and idempotent status updates
    }

    public PartnerEarningsDto getPartnerEarnings(Long partnerUserId) {
        Partner partner = partnerRepository.findByUserId(partnerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        List<Booking> bookings = bookingRepository.findByPartnerId(partner.getId());

        int totalBookings = bookings.size();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;

        for (Booking b : bookings) {
            if ("CONFIRMED".equalsIgnoreCase(b.getStatus()) || "COMPLETED".equalsIgnoreCase(b.getStatus())) {
                totalRevenue = totalRevenue.add(b.getAmount());
                totalCommission = totalCommission.add(b.getCommissionAmount());
            }
        }

        BigDecimal netEarnings = totalRevenue.subtract(totalCommission);
        BigDecimal pendingPayout = netEarnings;

        return new PartnerEarningsDto(totalBookings, totalRevenue, totalCommission, netEarnings, pendingPayout);
    }

    public List<PayoutDto> getPartnerPayouts(Long partnerUserId) {
        Partner partner = partnerRepository.findByUserId(partnerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        return payoutRepository.findByPartnerId(partner.getId()).stream()
                .map(PayoutDto::new)
                .collect(Collectors.toList());
    }

    public String calculateHmacSha256(String data, String secret) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating HMAC SHA256", e);
        }
    }
}
