package com.hyderabad.booking.controller;

import com.hyderabad.booking.dto.CreateOrderRequest;
import com.hyderabad.booking.dto.CreateOrderResponse;
import com.hyderabad.booking.dto.PaymentDto;
import com.hyderabad.booking.dto.VerifyPaymentRequest;
import com.hyderabad.booking.security.UserPrincipal;
import com.hyderabad.booking.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse response = paymentService.createOrder(principal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentDto> verifyPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VerifyPaymentRequest request) {
        PaymentDto paymentDto = paymentService.verifyPayment(principal.getId(), request);
        return ResponseEntity.ok(paymentDto);
    }
}
