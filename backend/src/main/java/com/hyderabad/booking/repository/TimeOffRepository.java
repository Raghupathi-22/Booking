package com.hyderabad.booking.repository;

import com.hyderabad.booking.model.TimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeOffRepository extends JpaRepository<TimeOff, Long> {
    List<TimeOff> findByPartnerId(Long partnerId);
    List<TimeOff> findByPartnerIdAndStartAtBeforeAndEndAtAfter(Long partnerId, LocalDateTime end, LocalDateTime start);
}
