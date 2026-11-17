package com.hyderabad.booking.repository;

import com.hyderabad.booking.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByPartnerId(Long partnerId);
    List<ServiceEntity> findByPartnerIdAndActiveTrue(Long partnerId);
}
