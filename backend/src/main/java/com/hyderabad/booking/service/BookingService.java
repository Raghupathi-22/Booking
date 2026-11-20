package com.hyderabad.booking.service;

import com.hyderabad.booking.dto.BookingDto;
import com.hyderabad.booking.dto.BookingRequest;
import com.hyderabad.booking.exception.BadRequestException;
import com.hyderabad.booking.exception.ResourceNotFoundException;
import com.hyderabad.booking.model.*;
import com.hyderabad.booking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PartnerRepository partnerRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final TimeOffRepository timeOffRepository;

    public BookingService(BookingRepository bookingRepository,
                          PartnerRepository partnerRepository,
                          ServiceRepository serviceRepository,
                          UserRepository userRepository,
                          TimeOffRepository timeOffRepository) {
        this.bookingRepository = bookingRepository;
        this.partnerRepository = partnerRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.timeOffRepository = timeOffRepository;
    }

    @Transactional
    public List<BookingDto> createBooking(Long customerId, BookingRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Partner partner = partnerRepository.findByIdForUpdate(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        if (!"APPROVED".equals(partner.getStatus())) {
            throw new BadRequestException("Cannot book with a partner who is not approved");
        }

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new BadRequestException("Service does not belong to this partner");
        }

        String bookingType = request.getBookingType() != null ? request.getBookingType() : "SINGLE";
        BigDecimal amount = service.getPrice();

        // Critical Rule 3: Free DEMO class bookings for tuition
        if ("DEMO".equalsIgnoreCase(bookingType)) {
            if (!"TUITION".equalsIgnoreCase(service.getCategory())) {
                throw new BadRequestException("Demo bookings are only available for tuition services");
            }
            amount = BigDecimal.ZERO;
        }

        BigDecimal commissionRate = partner.getCommissionPercent() != null ? partner.getCommissionPercent() : new BigDecimal("10.00");
        BigDecimal commissionAmount = amount.multiply(commissionRate).divide(new BigDecimal("100"));

        List<LocalDateTime> startTimes = new ArrayList<>();
        startTimes.add(request.getStartAt());

        // Handle RECURRING bookings (e.g. weekly for 4 weeks if recurrence rule specified or default weekly)
        if ("RECURRING".equalsIgnoreCase(bookingType)) {
            int weeks = 4; // default recurring 4 weeks
            if (request.getRecurrenceRule() != null && request.getRecurrenceRule().contains("WEEKS=")) {
                try {
                    String[] parts = request.getRecurrenceRule().split("WEEKS=");
                    weeks = Integer.parseInt(parts[1].split(";")[0]);
                } catch (Exception ignored) {}
            }
            for (int i = 1; i < weeks; i++) {
                startTimes.add(request.getStartAt().plusWeeks(i));
            }
        }

        List<Booking> createdBookings = new ArrayList<>();
        int durationMins = service.getDurationMinutes();

        for (LocalDateTime startAt : startTimes) {
            LocalDateTime endAt = startAt.plusMinutes(durationMins);

            // Check time-off conflict
            List<TimeOff> timeOffs = timeOffRepository.findByPartnerIdAndStartAtBeforeAndEndAtAfter(partner.getId(), endAt, startAt);
            if (!timeOffs.isEmpty()) {
                throw new BadRequestException("Partner is on time-off during the requested time slot: " + startAt);
            }

            // Critical Rule 1: Double-booking protection using pessimistic write lock
            List<Booking> overlapping = bookingRepository.findOverlappingBookingsForUpdate(partner.getId(), startAt, endAt);
            if (!overlapping.isEmpty()) {
                throw new BadRequestException("Slot is already booked: " + startAt);
            }

            Booking booking = new Booking(
                    customer,
                    partner,
                    service,
                    startAt,
                    endAt,
                    "PENDING",
                    bookingType,
                    request.getRecurrenceRule(),
                    amount,
                    commissionAmount,
                    amount.compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "PENDING"
            );

            bookingRepository.save(booking);
            createdBookings.add(booking);
        }

        return createdBookings.stream().map(BookingDto::new).collect(Collectors.toList());
    }

    public List<BookingDto> getUserBookings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if ("PARTNER".equalsIgnoreCase(user.getRole())) {
            Partner partner = partnerRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));
            return bookingRepository.findByPartnerId(partner.getId()).stream()
                    .map(BookingDto::new)
                    .collect(Collectors.toList());
        } else if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return bookingRepository.findAll().stream()
                    .map(BookingDto::new)
                    .collect(Collectors.toList());
        } else {
            return bookingRepository.findByCustomerId(userId).stream()
                    .map(BookingDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public BookingDto cancelBooking(Long userId, Long bookingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean isCustomer = booking.getCustomer().getId().equals(userId);
        boolean isPartner = booking.getPartner().getUser().getId().equals(userId);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        if (!isCustomer && !isPartner && !isAdmin) {
            throw new BadRequestException("Unauthorized to cancel this booking");
        }

        // Free cancellation up to 2 hours before start
        if (isCustomer && !isAdmin) {
            if (LocalDateTime.now().plusHours(2).isAfter(booking.getStartAt())) {
                // Within 2 hours - cancellation policy applies (still can cancel, but note or policy)
                // We allow cancellation but can flag payment status if needed
            }
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        return new BookingDto(booking);
    }

    /**
     * Partner actions on their own bookings: ACCEPT (confirm a paid/free pending booking),
     * REJECT (cancel) and COMPLETE (mark a confirmed booking as done).
     */
    @Transactional
    public BookingDto updateStatusAsPartner(Long partnerUserId, Long bookingId, String action) {
        Partner partner = partnerRepository.findByUserId(partnerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getPartner().getId().equals(partner.getId())) {
            throw new BadRequestException("Booking does not belong to this partner");
        }

        String status = booking.getStatus();
        switch (action.toUpperCase()) {
            case "ACCEPT" -> {
                if (!"PENDING".equals(status)) {
                    throw new BadRequestException("Only pending bookings can be accepted");
                }
                if (!"PAID".equals(booking.getPaymentStatus())) {
                    throw new BadRequestException("Booking is still awaiting payment");
                }
                booking.setStatus("CONFIRMED");
            }
            case "REJECT" -> {
                if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
                    throw new BadRequestException("Booking can no longer be rejected");
                }
                booking.setStatus("CANCELLED");
            }
            case "COMPLETE" -> {
                if (!"CONFIRMED".equals(status)) {
                    throw new BadRequestException("Only confirmed bookings can be completed");
                }
                booking.setStatus("COMPLETED");
            }
            default -> throw new BadRequestException("Unknown action: " + action);
        }

        bookingRepository.save(booking);
        return new BookingDto(booking);
    }
}
