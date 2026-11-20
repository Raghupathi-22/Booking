package com.hyderabad.booking.service;

import com.hyderabad.booking.dto.*;
import com.hyderabad.booking.exception.ResourceNotFoundException;
import com.hyderabad.booking.model.*;
import com.hyderabad.booking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerDocumentRepository partnerDocumentRepository;
    private final ServiceRepository serviceRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final TimeOffRepository timeOffRepository;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    public AdminService(UserRepository userRepository,
                        PartnerRepository partnerRepository,
                        PartnerDocumentRepository partnerDocumentRepository,
                        ServiceRepository serviceRepository,
                        WorkingHoursRepository workingHoursRepository,
                        TimeOffRepository timeOffRepository,
                        ReviewRepository reviewRepository,
                        BookingRepository bookingRepository,
                        PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.partnerRepository = partnerRepository;
        this.partnerDocumentRepository = partnerDocumentRepository;
        this.serviceRepository = serviceRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.timeOffRepository = timeOffRepository;
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
    }

    public AdminStatsDto getStats() {
        long totalUsers = userRepository.count();
        List<Partner> partners = partnerRepository.findAll();
        long totalPartners = partners.size();
        long pendingPartners = partners.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getStatus())).count();
        long approvedPartners = partners.stream().filter(p -> "APPROVED".equalsIgnoreCase(p.getStatus())).count();

        List<Booking> bookings = bookingRepository.findAll();
        long totalBookings = bookings.size();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;

        for (Booking b : bookings) {
            if ("CONFIRMED".equalsIgnoreCase(b.getStatus()) || "COMPLETED".equalsIgnoreCase(b.getStatus())) {
                if (b.getAmount() != null) {
                    totalRevenue = totalRevenue.add(b.getAmount());
                }
                if (b.getCommissionAmount() != null) {
                    totalCommission = totalCommission.add(b.getCommissionAmount());
                }
            }
        }

        return new AdminStatsDto(totalUsers, totalPartners, pendingPartners, approvedPartners, totalBookings, totalRevenue, totalCommission);
    }

    public List<PartnerDetailDto> getPartners(String status) {
        List<Partner> partners = partnerRepository.findAll();
        if (status != null && !status.isBlank()) {
            partners = partners.stream()
                    .filter(p -> status.equalsIgnoreCase(p.getStatus()))
                    .collect(Collectors.toList());
        }

        return partners.stream().map(partner -> {
            List<ServiceDto> services = serviceRepository.findByPartnerId(partner.getId()).stream().map(ServiceDto::new).collect(Collectors.toList());
            List<WorkingHoursDto> workingHours = workingHoursRepository.findByPartnerId(partner.getId()).stream().map(WorkingHoursDto::new).collect(Collectors.toList());
            List<TimeOffDto> timeOff = timeOffRepository.findByPartnerId(partner.getId()).stream().map(TimeOffDto::new).collect(Collectors.toList());
            List<ReviewDto> reviews = reviewRepository.findByPartnerId(partner.getId()).stream().map(ReviewDto::new).collect(Collectors.toList());
            List<PartnerDocumentDto> documents = partnerDocumentRepository.findByPartnerId(partner.getId()).stream().map(PartnerDocumentDto::new).collect(Collectors.toList());
            return new PartnerDetailDto(partner, services, workingHours, timeOff, reviews, documents);
        }).collect(Collectors.toList());
    }

    @Transactional
    public PartnerDto updatePartnerStatus(Long partnerId, String status) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        partner.setStatus(status.toUpperCase());
        partnerRepository.save(partner);
        return new PartnerDto(partner);
    }

    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(BookingDto::new)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAllWithBooking().stream()
                .map(PaymentDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentDto processRefund(RefundRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        payment.setRefundedAmount(request.getRefundAmount());
        payment.setStatus("REFUNDED");
        paymentRepository.save(payment);

        booking.setStatus("CANCELLED");
        booking.setPaymentStatus("REFUNDED");
        bookingRepository.save(booking);

        return new PaymentDto(payment);
    }
}
