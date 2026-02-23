package com.socialmedia.user.controller;

import com.socialmedia.user.dto.LoginRequest;
import com.socialmedia.user.dto.LoginResponse;
import com.socialmedia.user.dto.ApiResponse;
import com.socialmedia.user.dto.RegisterUserRequest;
import com.socialmedia.user.dto.RegisterUserResponse;
import com.socialmedia.user.dto.UpdateBioRequest;
import com.socialmedia.user.dto.UserProfileResponse;
import com.socialmedia.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterUserResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Register request for username={}, email={}", request.getUsername(), request.getEmail());
        RegisterUserResponse response = userService.register(request);
        ApiResponse<RegisterUserResponse> apiResponse = ApiResponse.<RegisterUserResponse>builder()
                .success(true)
                .data(response)
                .message("User registered successfully")
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());
        LoginResponse response = userService.login(request);
        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .success(true)
                .data(response)
                .message("Login successful")
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@PathVariable @Min(1) Long id) {
        log.info("Fetching profile for userId={}", id);
        UserProfileResponse response = userService.getProfile(id);
        ApiResponse<UserProfileResponse> apiResponse = ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .data(response)
                .message("User profile fetched")
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}/bio")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateBio(@PathVariable @Min(1) Long id, @Valid @RequestBody UpdateBioRequest request) {
        log.info("Updating bio for userId={}", id);
        UserProfileResponse response = userService.updateBio(id, request);
        ApiResponse<UserProfileResponse> apiResponse = ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .data(response)
                .message("User bio updated")
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
