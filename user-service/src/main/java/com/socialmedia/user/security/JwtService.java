package com.socialmedia.user.security;

public interface JwtService {

    String generateToken(Long userId, String username);
    Long extractUserId(String token);
    String extractUsername(String token);
    boolean isTokenValid(String token, Long userId);
}

