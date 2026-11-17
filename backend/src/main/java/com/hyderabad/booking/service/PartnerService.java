package com.hyderabad.booking.service;

import com.hyderabad.booking.dto.*;
import com.hyderabad.booking.exception.BadRequestException;
import com.hyderabad.booking.exception.ResourceNotFoundException;
import com.hyderabad.booking.model.*;
import com.hyderabad.booking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerDocumentRepository partnerDocumentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final TimeOffRepository timeOffRepository;

    public PartnerService(PartnerRepository partnerRepository,
                          PartnerDocumentRepository partnerDocumentRepository,
                          UserRepository userRepository,
                          ServiceRepository serviceRepository,
                          WorkingHoursRepository workingHoursRepository,
                          TimeOffRepository timeOffRepository) {
        this.partnerRepository = partnerRepository;
        this.partnerDocumentRepository = partnerDocumentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.timeOffRepository = timeOffRepository;
    }

    @Transactional
    public PartnerDto registerPartner(Long userId, PartnerRegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Partner partner = partnerRepository.findByUserId(userId).orElseGet(() -> {
            Partner p = new Partner();
            p.setUser(user);
            return p;
        });

        partner.setType(request.getType());
        partner.setBusinessName(request.getBusinessName());
        partner.setDescription(request.getDescription());
        partner.setAddress(request.getAddress());
        partner.setLat(request.getLat());
        partner.setLng(request.getLng());
        partner.setArea(request.getArea());
        if (partner.getStatus() == null || partner.getStatus().isBlank()) {
            partner.setStatus("PENDING");
        }

        partnerRepository.save(partner);

        if (!"PARTNER".equals(user.getRole())) {
            user.setRole("PARTNER");
            userRepository.save(user);
        }

        return new PartnerDto(partner);
    }

    @Transactional
    public PartnerDocumentDto uploadDocument(Long userId, PartnerDocumentRequest request) {
        Partner partner = getPartnerByUserId(userId);
        PartnerDocument document = new PartnerDocument(partner, request.getDocType(), request.getUrl());
        partnerDocumentRepository.save(document);
        return new PartnerDocumentDto(document);
    }

    public PartnerDto getPartnerProfile(Long userId) {
        Partner partner = getPartnerByUserId(userId);
        return new PartnerDto(partner);
    }

    public Partner getPartnerByUserId(Long userId) {
        return partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found. Please register partner first."));
    }

    // --- Services Management ---

    @Transactional
    public ServiceDto createService(Long userId, ServiceRequest request) {
        Partner partner = getPartnerByUserId(userId);
        ServiceEntity service = new ServiceEntity(
                partner,
                request.getCategory(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getDurationMinutes(),
                request.getMode(),
                request.getActive()
        );
        serviceRepository.save(service);
        return new ServiceDto(service);
    }

    public List<ServiceDto> getPartnerServices(Long userId) {
        Partner partner = getPartnerByUserId(userId);
        return serviceRepository.findByPartnerId(partner.getId()).stream()
                .map(ServiceDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceDto updateService(Long userId, Long serviceId, ServiceRequest request) {
        Partner partner = getPartnerByUserId(userId);
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new BadRequestException("Unauthorized to update this service");
        }

        service.setCategory(request.getCategory());
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setMode(request.getMode());
        if (request.getActive() != null) {
            service.setActive(request.getActive());
        }

        serviceRepository.save(service);
        return new ServiceDto(service);
    }

    @Transactional
    public void deleteService(Long userId, Long serviceId) {
        Partner partner = getPartnerByUserId(userId);
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new BadRequestException("Unauthorized to delete this service");
        }

        serviceRepository.delete(service);
    }

    // --- Working Hours Management ---

    @Transactional
    public List<WorkingHoursDto> setWorkingHours(Long userId, List<WorkingHoursRequest> requests) {
        Partner partner = getPartnerByUserId(userId);
        workingHoursRepository.deleteByPartnerId(partner.getId());

        List<WorkingHours> workingHoursList = requests.stream().map(req -> {
            WorkingHours wh = new WorkingHours();
            wh.setPartner(partner);
            wh.setDayOfWeek(req.getDayOfWeek());
            wh.setOpenTime(req.getOpenTime());
            wh.setCloseTime(req.getCloseTime());
            return wh;
        }).collect(Collectors.toList());

        workingHoursRepository.saveAll(workingHoursList);

        return workingHoursList.stream().map(WorkingHoursDto::new).collect(Collectors.toList());
    }

    public List<WorkingHoursDto> getWorkingHours(Long userId) {
        Partner partner = getPartnerByUserId(userId);
        return workingHoursRepository.findByPartnerId(partner.getId()).stream()
                .map(WorkingHoursDto::new)
                .collect(Collectors.toList());
    }

    // --- Time Off Management ---

    @Transactional
    public TimeOffDto addTimeOff(Long userId, TimeOffRequest request) {
        Partner partner = getPartnerByUserId(userId);
        TimeOff timeOff = new TimeOff(partner, request.getStartAt(), request.getEndAt());
        timeOffRepository.save(timeOff);
        return new TimeOffDto(timeOff);
    }

    public List<TimeOffDto> getTimeOff(Long userId) {
        Partner partner = getPartnerByUserId(userId);
        return timeOffRepository.findByPartnerId(partner.getId()).stream()
                .map(TimeOffDto::new)
                .collect(Collectors.toList());
    }
}
