package com.hyderabad.booking.controller;

import com.hyderabad.booking.dto.UpdateUserRequest;
import com.hyderabad.booking.dto.UserDto;
import com.hyderabad.booking.security.UserPrincipal;
import com.hyderabad.booking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        UserDto userDto = userService.getCurrentUser(principal.getId());
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto userDto = userService.updateUser(principal.getId(), request);
        return ResponseEntity.ok(userDto);
    }
}
