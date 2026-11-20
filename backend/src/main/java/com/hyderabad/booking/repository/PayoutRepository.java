package com.hyderabad.booking.repository;

import com.hyderabad.booking.model.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {
    List<Payout> findByPartnerId(Long partnerId);
}
