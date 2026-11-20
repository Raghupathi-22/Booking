package com.hyderabad.booking.controller;

import com.hyderabad.booking.dto.*;
import com.hyderabad.booking.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminApiController {

    private final AdminService adminService;

    public AdminApiController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/partners")
    public ResponseEntity<List<PartnerDetailDto>> getPartners(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getPartners(status));
    }

    @PostMapping("/partners/{id}/approve")
    public ResponseEntity<PartnerDto> approvePartner(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.updatePartnerStatus(id, "APPROVED"));
    }

    @PostMapping("/partners/{id}/reject")
    public ResponseEntity<PartnerDto> rejectPartner(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.updatePartnerStatus(id, "REJECTED"));
    }

    @PostMapping("/partners/{id}/suspend")
    public ResponseEntity<PartnerDto> suspendPartner(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.updatePartnerStatus(id, "SUSPENDED"));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingDto>> getBookings() {
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentDto>> getPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    @PostMapping("/refunds")
    public ResponseEntity<PaymentDto> processRefund(@Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(adminService.processRefund(request));
    }
}
