package com.hyderabad.booking.repository;

import com.hyderabad.booking.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPartnerId(Long partnerId);

    boolean existsByBookingId(Long bookingId);
}
