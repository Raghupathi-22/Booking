package com.hyderabad.booking.repository;

import com.hyderabad.booking.model.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
    List<WorkingHours> findByPartnerId(Long partnerId);
    List<WorkingHours> findByPartnerIdAndDayOfWeek(Long partnerId, String dayOfWeek);
    void deleteByPartnerId(Long partnerId);
}
