package com.hyderabad.booking.repository;

import com.hyderabad.booking.model.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerId(Long customerId);
    List<Booking> findByPartnerId(Long partnerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.partner.id = :partnerId AND b.status IN ('PENDING', 'CONFIRMED') AND b.startAt < :endAt AND b.endAt > :startAt")
    List<Booking> findOverlappingBookingsForUpdate(
            @Param("partnerId") Long partnerId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Query("SELECT b FROM Booking b WHERE b.partner.id = :partnerId AND b.status IN ('PENDING', 'CONFIRMED') AND b.startAt < :endAt AND b.endAt > :startAt")
    List<Booking> findOverlappingBookings(
            @Param("partnerId") Long partnerId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
