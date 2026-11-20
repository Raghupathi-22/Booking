package com.hyderabad.booking.controller;

import com.hyderabad.booking.dto.ReviewDto;
import com.hyderabad.booking.dto.ReviewRequest;
import com.hyderabad.booking.security.UserPrincipal;
import com.hyderabad.booking.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReviewRequest request) {
        ReviewDto dto = reviewService.createReview(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
