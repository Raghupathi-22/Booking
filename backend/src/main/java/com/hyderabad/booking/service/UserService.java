package com.hyderabad.booking.service;

import com.hyderabad.booking.dto.UpdateUserRequest;
import com.hyderabad.booking.dto.UserDto;
import com.hyderabad.booking.exception.ResourceNotFoundException;
import com.hyderabad.booking.model.User;
import com.hyderabad.booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserDto(user);
    }

    @Transactional
    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getFcmToken() != null) {
            user.setFcmToken(request.getFcmToken());
        }

        userRepository.save(user);
        return new UserDto(user);
    }
}
