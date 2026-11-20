package com.hyderabad.booking.service;

import com.hyderabad.booking.dto.ReviewDto;
import com.hyderabad.booking.dto.ReviewRequest;
import com.hyderabad.booking.exception.BadRequestException;
import com.hyderabad.booking.exception.ResourceNotFoundException;
import com.hyderabad.booking.model.Booking;
import com.hyderabad.booking.model.Partner;
import com.hyderabad.booking.model.Review;
import com.hyderabad.booking.repository.BookingRepository;
import com.hyderabad.booking.repository.PartnerRepository;
import com.hyderabad.booking.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final PartnerRepository partnerRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         BookingRepository bookingRepository,
                         PartnerRepository partnerRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.partnerRepository = partnerRepository;
    }

    /**
     * Business rule 7: reviews only after a COMPLETED booking, one review per booking.
     */
    @Transactional
    public ReviewDto createReview(Long customerId, ReviewRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Booking does not belong to this customer");
        }
        if (!"COMPLETED".equals(booking.getStatus())) {
            throw new BadRequestException("You can only review completed bookings");
        }
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new BadRequestException("This booking has already been reviewed");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setCustomer(booking.getCustomer());
        review.setPartner(booking.getPartner());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        // Keep the partner's average rating up to date (locked row to avoid lost updates)
        Partner partner = partnerRepository.findByIdForUpdate(booking.getPartner().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
        int oldCount = partner.getRatingCount() == null ? 0 : partner.getRatingCount();
        BigDecimal oldAvg = partner.getRatingAvg() == null ? BigDecimal.ZERO : partner.getRatingAvg();
        int newCount = oldCount + 1;
        BigDecimal total = oldAvg.multiply(BigDecimal.valueOf(oldCount))
                .add(BigDecimal.valueOf(request.getRating()));
        partner.setRatingAvg(total.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP));
        partner.setRatingCount(newCount);
        partnerRepository.save(partner);

        return new ReviewDto(review);
    }
}
