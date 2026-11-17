package com.hyderabad.booking.service;

import com.hyderabad.booking.dto.*;
import com.hyderabad.booking.exception.ResourceNotFoundException;
import com.hyderabad.booking.exception.UnauthorizedException;
import com.hyderabad.booking.model.User;
import com.hyderabad.booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final OtpService otpService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(OtpService otpService, JwtService jwtService, UserRepository userRepository) {
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public SendOtpResponse sendOtp(SendOtpRequest request) {
        String otp = otpService.generateAndSendOtp(request.getPhone());
        return new SendOtpResponse("OTP sent successfully", otp);
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        otpService.verifyOtp(request.getPhone(), request.getOtp());

        User user = userRepository.findByPhone(request.getPhone()).orElseGet(() -> {
            String name = request.getName() != null && !request.getName().isBlank() ? request.getName() : "User " + request.getPhone();
            String role = request.getRole() != null && !request.getRole().isBlank() ? request.getRole() : "CUSTOMER";
            User newUser = new User(request.getPhone(), name, role, null);
            return userRepository.save(newUser);
        });

        String accessToken = jwtService.generateAccessToken(user.getPhone(), user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getPhone(), user.getId());

        return new AuthResponse(accessToken, refreshToken, new UserDto(user));
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtService.isRefreshToken(token)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String phone = jwtService.extractPhone(token);
        Long userId = jwtService.extractUserId(token);

        User user = userRepository.findById(userId)
                .orElseGet(() -> userRepository.findByPhone(phone)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")));

        String accessToken = jwtService.generateAccessToken(user.getPhone(), user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getPhone(), user.getId());

        return new AuthResponse(accessToken, refreshToken, new UserDto(user));
    }
}
