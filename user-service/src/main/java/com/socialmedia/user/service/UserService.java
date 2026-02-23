package com.socialmedia.user.service;

import com.socialmedia.user.dto.LoginRequest;
import com.socialmedia.user.dto.LoginResponse;
import com.socialmedia.user.dto.RegisterUserRequest;
import com.socialmedia.user.dto.RegisterUserResponse;
import com.socialmedia.user.dto.UserProfileResponse;
import com.socialmedia.user.dto.UpdateBioRequest;
import com.socialmedia.user.entity.User;
import com.socialmedia.user.repository.UserRepository;
import com.socialmedia.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterUserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username {} already in use", request.getUsername());
            throw new IllegalArgumentException("Username already in use");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email {} already in use", request.getEmail());
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBio(request.getBio());

        User saved = userRepository.save(user);
        log.info("User registered id={}, username={}", saved.getId(), saved.getUsername());
        return new RegisterUserResponse(saved.getId(), "User created");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> {
                    log.warn("Login failed: email {} not found", request.getEmail());
                    return new IllegalArgumentException("Invalid credentials");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password for email {}", request.getEmail());
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        log.info("Login successful for userId={}, username={}", user.getId(), user.getUsername());
        return new LoginResponse(token);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
                    log.warn("Profile fetch failed: userId {} not found", id);
                    return new IllegalArgumentException("User not found");
                });

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setBio(user.getBio());
        response.setCreatedAt(user.getCreatedAt());
        log.debug("Profile fetched for userId={}", id);
        return response;
    }

    @Transactional
    public UserProfileResponse updateBio(Long id, UpdateBioRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> {
                    log.warn("Update bio failed: userId {} not found", id);
                    return new IllegalArgumentException("User not found");
                });

        user.setBio(request.getBio());
        User saved = userRepository.save(user);
        log.info("Updated bio for userId={}", saved.getId());

        UserProfileResponse response = new UserProfileResponse();
        response.setId(saved.getId());
        response.setUsername(saved.getUsername());
        response.setEmail(saved.getEmail());
        response.setBio(saved.getBio());
        response.setCreatedAt(saved.getCreatedAt());
        return response;
    }
}
