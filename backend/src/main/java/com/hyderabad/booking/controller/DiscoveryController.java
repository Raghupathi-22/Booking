package com.hyderabad.booking.controller;

import com.hyderabad.booking.dto.PartnerDetailDto;
import com.hyderabad.booking.dto.PartnerDto;
import com.hyderabad.booking.dto.SlotDto;
import com.hyderabad.booking.service.DiscoveryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/partners")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping
    public ResponseEntity<List<PartnerDto>> searchPartners(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) BigDecimal lat,
            @RequestParam(required = false) BigDecimal lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) String sort) {
        List<PartnerDto> partners = discoveryService.searchPartners(category, area, lat, lng, radiusKm, minPrice, maxPrice, minRating, sort);
        return ResponseEntity.ok(partners);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerDetailDto> getPartnerDetail(@PathVariable Long id) {
        PartnerDetailDto detail = discoveryService.getPartnerDetail(id);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<List<SlotDto>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long serviceId) {
        List<SlotDto> slots = discoveryService.getAvailableSlots(id, serviceId, date);
        return ResponseEntity.ok(slots);
    }
}
