package com.hyderabad.booking.controller;

import com.hyderabad.booking.dto.AdminStatsDto;
import com.hyderabad.booking.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        try {
            AdminStatsDto stats = adminService.getStats();
            model.addAttribute("totalUsers", stats.getTotalUsers());
            model.addAttribute("pendingPartners", stats.getPendingPartners());
            model.addAttribute("approvedPartners", stats.getApprovedPartners());
            model.addAttribute("totalBookings", stats.getTotalBookings());
            model.addAttribute("totalRevenue", stats.getTotalRevenue() != null ? stats.getTotalRevenue().toString() : "0.00");
            model.addAttribute("totalCommissionEarned", stats.getTotalCommissionEarned() != null ? stats.getTotalCommissionEarned().toString() : "0.00");
            model.addAttribute("appName", "Hyderabad Booking Marketplace");
        } catch (Exception e) {
            log.error("Error building dashboard stats", e);
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("pendingPartners", 0L);
            model.addAttribute("approvedPartners", 0L);
            model.addAttribute("totalBookings", 0L);
            model.addAttribute("totalRevenue", "0.00");
            model.addAttribute("totalCommissionEarned", "0.00");
            model.addAttribute("appName", "Hyderabad Booking Marketplace");
        }
        return "admin/index";
    }

    @GetMapping("/partners")
    public String partners(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("partnerList", adminService.getPartners(status));
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("appName", "Hyderabad Booking Marketplace");
        return "admin/partners";
    }

    @PostMapping("/partners/{id}/status")
    public String updatePartnerStatus(@PathVariable Long id, @RequestParam String status) {
        adminService.updatePartnerStatus(id, status);
        return "redirect:/admin/partners";
    }

    @GetMapping("/bookings")
    public String bookings(Model model) {
        model.addAttribute("bookingList", adminService.getAllBookings());
        model.addAttribute("appName", "Hyderabad Booking Marketplace");
        return "admin/bookings";
    }

    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("paymentList", adminService.getAllPayments());
        model.addAttribute("appName", "Hyderabad Booking Marketplace");
        return "admin/payments";
    }
}
