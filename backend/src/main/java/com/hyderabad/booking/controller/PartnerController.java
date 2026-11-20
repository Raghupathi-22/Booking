package com.hyderabad.booking.controller;

import com.hyderabad.booking.dto.*;
import com.hyderabad.booking.security.UserPrincipal;
import com.hyderabad.booking.service.BookingService;
import com.hyderabad.booking.service.PartnerService;
import com.hyderabad.booking.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/partner")
public class PartnerController {

    private final PartnerService partnerService;
    private final PaymentService paymentService;
    private final BookingService bookingService;

    public PartnerController(PartnerService partnerService, PaymentService paymentService, BookingService bookingService) {
        this.partnerService = partnerService;
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    @PostMapping("/register")
    public ResponseEntity<PartnerDto> registerPartner(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PartnerRegisterRequest request) {
        PartnerDto partnerDto = partnerService.registerPartner(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerDto);
    }

    @PostMapping("/documents")
    public ResponseEntity<PartnerDocumentDto> uploadDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PartnerDocumentRequest request) {
        PartnerDocumentDto docDto = partnerService.uploadDocument(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(docDto);
    }

    @GetMapping("/me")
    public ResponseEntity<PartnerDto> getPartnerProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        PartnerDto partnerDto = partnerService.getPartnerProfile(principal.getId());
        return ResponseEntity.ok(partnerDto);
    }

    // --- Services Endpoints ---

    @GetMapping("/services")
    public ResponseEntity<List<ServiceDto>> getPartnerServices(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(partnerService.getPartnerServices(principal.getId()));
    }

    @PostMapping("/services")
    public ResponseEntity<ServiceDto> createService(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ServiceRequest request) {
        ServiceDto serviceDto = partnerService.createService(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceDto);
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<ServiceDto> updateService(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request) {
        ServiceDto serviceDto = partnerService.updateService(principal.getId(), id, request);
        return ResponseEntity.ok(serviceDto);
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteService(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        partnerService.deleteService(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    // --- Working Hours Endpoints ---

    @PutMapping("/working-hours")
    public ResponseEntity<List<WorkingHoursDto>> setWorkingHours(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody List<WorkingHoursRequest> requests) {
        List<WorkingHoursDto> whList = partnerService.setWorkingHours(principal.getId(), requests);
        return ResponseEntity.ok(whList);
    }

    @GetMapping("/working-hours")
    public ResponseEntity<List<WorkingHoursDto>> getWorkingHours(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(partnerService.getWorkingHours(principal.getId()));
    }

    // --- Time Off Endpoints ---

    @PostMapping("/time-off")
    public ResponseEntity<TimeOffDto> addTimeOff(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TimeOffRequest request) {
        TimeOffDto timeOffDto = partnerService.addTimeOff(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(timeOffDto);
    }

    @GetMapping("/time-off")
    public ResponseEntity<List<TimeOffDto>> getTimeOff(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(partnerService.getTimeOff(principal.getId()));
    }

    // --- Earnings & Payouts Endpoints ---

    @GetMapping("/earnings")
    public ResponseEntity<PartnerEarningsDto> getEarnings(
            @AuthenticationPrincipal UserPrincipal principal) {
        PartnerEarningsDto earnings = paymentService.getPartnerEarnings(principal.getId());
        return ResponseEntity.ok(earnings);
    }

    @GetMapping("/payouts")
    public ResponseEntity<List<PayoutDto>> getPayouts(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PayoutDto> payouts = paymentService.getPartnerPayouts(principal.getId());
        return ResponseEntity.ok(payouts);
    }

    // --- Booking actions (partner side) ---
    @PostMapping("/bookings/{id}/accept")
    public ResponseEntity<BookingDto> acceptBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.updateStatusAsPartner(principal.getId(), id, "ACCEPT"));
    }

    @PostMapping("/bookings/{id}/reject")
    public ResponseEntity<BookingDto> rejectBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.updateStatusAsPartner(principal.getId(), id, "REJECT"));
    }

    @PostMapping("/bookings/{id}/complete")
    public ResponseEntity<BookingDto> completeBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.updateStatusAsPartner(principal.getId(), id, "COMPLETE"));
    }
}
