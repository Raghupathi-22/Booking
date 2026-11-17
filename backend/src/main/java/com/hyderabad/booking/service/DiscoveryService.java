package com.hyderabad.booking.service;

import com.hyderabad.booking.dto.*;
import com.hyderabad.booking.exception.ResourceNotFoundException;
import com.hyderabad.booking.model.*;
import com.hyderabad.booking.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscoveryService {

    private final PartnerRepository partnerRepository;
    private final ServiceRepository serviceRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final TimeOffRepository timeOffRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    private static final ZoneId KOLKATA_ZONE = ZoneId.of("Asia/Kolkata");

    public DiscoveryService(PartnerRepository partnerRepository,
                            ServiceRepository serviceRepository,
                            WorkingHoursRepository workingHoursRepository,
                            TimeOffRepository timeOffRepository,
                            BookingRepository bookingRepository,
                            ReviewRepository reviewRepository) {
        this.partnerRepository = partnerRepository;
        this.serviceRepository = serviceRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.timeOffRepository = timeOffRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<PartnerDto> searchPartners(String category, String area, BigDecimal lat, BigDecimal lng, Double radiusKm, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minRating, String sort) {
        List<Partner> partners = partnerRepository.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .collect(Collectors.toList());

        // Filter by area
        if (area != null && !area.isBlank()) {
            partners = partners.stream()
                    .filter(p -> p.getArea() != null && p.getArea().toLowerCase().contains(area.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filter by category (via partner type or partner's services category)
        if (category != null && !category.isBlank()) {
            partners = partners.stream().filter(p -> {
                boolean matchType = category.equalsIgnoreCase(p.getType()) ||
                        (category.equalsIgnoreCase("TUITION") && "TUTOR".equalsIgnoreCase(p.getType()));
                List<ServiceEntity> services = serviceRepository.findByPartnerIdAndActiveTrue(p.getId());
                boolean matchServiceCategory = services.stream().anyMatch(s -> category.equalsIgnoreCase(s.getCategory()));
                return matchType || matchServiceCategory;
            }).collect(Collectors.toList());
        }

        // Filter by rating
        if (minRating != null) {
            partners = partners.stream()
                    .filter(p -> p.getRatingAvg() != null && p.getRatingAvg().compareTo(minRating) >= 0)
                    .collect(Collectors.toList());
        }

        // Filter by lat/lng radius (Haversine formula approximation)
        if (lat != null && lng != null && radiusKm != null) {
            partners = partners.stream().filter(p -> {
                if (p.getLat() == null || p.getLng() == null) return false;
                double dist = calculateDistance(lat.doubleValue(), lng.doubleValue(), p.getLat().doubleValue(), p.getLng().doubleValue());
                return dist <= radiusKm;
            }).collect(Collectors.toList());
        }

        // Filter by price range (if services match price)
        if (minPrice != null || maxPrice != null) {
            partners = partners.stream().filter(p -> {
                List<ServiceEntity> services = serviceRepository.findByPartnerIdAndActiveTrue(p.getId());
                if (services.isEmpty()) return false;
                return services.stream().anyMatch(s -> {
                    boolean ok = true;
                    if (minPrice != null && s.getPrice().compareTo(minPrice) < 0) ok = false;
                    if (maxPrice != null && s.getPrice().compareTo(maxPrice) > 0) ok = false;
                    return ok;
                });
            }).collect(Collectors.toList());
        }

        // Sorting
        if ("rating".equalsIgnoreCase(sort)) {
            partners.sort(Comparator.comparing(Partner::getRatingAvg, Comparator.nullsLast(Comparator.reverseOrder())));
        } else if ("price".equalsIgnoreCase(sort)) {
            partners.sort(Comparator.comparing(p -> {
                List<ServiceEntity> services = serviceRepository.findByPartnerIdAndActiveTrue(p.getId());
                return services.stream().map(ServiceEntity::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            }));
        }

        return partners.stream().map(PartnerDto::new).collect(Collectors.toList());
    }

    public PartnerDetailDto getPartnerDetail(Long partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        if (!"APPROVED".equals(partner.getStatus())) {
            throw new ResourceNotFoundException("Partner not found or not approved");
        }

        List<ServiceDto> services = serviceRepository.findByPartnerIdAndActiveTrue(partnerId).stream()
                .map(ServiceDto::new)
                .collect(Collectors.toList());

        List<WorkingHoursDto> workingHours = workingHoursRepository.findByPartnerId(partnerId).stream()
                .map(WorkingHoursDto::new)
                .collect(Collectors.toList());

        List<TimeOffDto> timeOff = timeOffRepository.findByPartnerId(partnerId).stream()
                .map(TimeOffDto::new)
                .collect(Collectors.toList());

        List<ReviewDto> reviews = reviewRepository.findByPartnerId(partnerId).stream()
                .map(ReviewDto::new)
                .collect(Collectors.toList());

        return new PartnerDetailDto(partner, services, workingHours, timeOff, reviews);
    }

    public List<SlotDto> getAvailableSlots(Long partnerId, Long serviceId, LocalDate date) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (!service.getPartner().getId().equals(partnerId)) {
            throw new ResourceNotFoundException("Service does not belong to this partner");
        }

        String dayOfWeekStr = date.getDayOfWeek().name();
        List<WorkingHours> workingHoursList = workingHoursRepository.findByPartnerIdAndDayOfWeek(partnerId, dayOfWeekStr);
        if (workingHoursList.isEmpty()) {
            return new ArrayList<>();
        }

        List<TimeOff> timeOffList = timeOffRepository.findByPartnerId(partnerId);
        List<Booking> bookings = bookingRepository.findOverlappingBookings(
                partnerId,
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX)
        );

        int durationMins = service.getDurationMinutes();
        List<SlotDto> availableSlots = new ArrayList<>();

        for (WorkingHours wh : workingHoursList) {
            LocalTime current = wh.getOpenTime();
            LocalTime close = wh.getCloseTime();

            while (current.plusMinutes(durationMins).compareTo(close) <= 0) {
                LocalDateTime slotStartLocal = LocalDateTime.of(date, current);
                LocalDateTime slotEndLocal = slotStartLocal.plusMinutes(durationMins);

                // Convert to UTC/Instant if needed for comparison, or compare LocalDateTime directly (assuming stored in UTC)
                boolean isBlocked = false;

                // Check time off overlap
                for (TimeOff to : timeOffList) {
                    if (slotStartLocal.isBefore(to.getEndAt()) && slotEndLocal.isAfter(to.getStartAt())) {
                        isBlocked = true;
                        break;
                    }
                }

                // Check booking overlap
                for (Booking b : bookings) {
                    if (slotStartLocal.isBefore(b.getEndAt()) && slotEndLocal.isAfter(b.getStartAt())) {
                        isBlocked = true;
                        break;
                    }
                }

                if (!isBlocked) {
                    availableSlots.add(new SlotDto(slotStartLocal, slotEndLocal));
                }

                current = current.plusMinutes(durationMins);
            }
        }

        return availableSlots;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
