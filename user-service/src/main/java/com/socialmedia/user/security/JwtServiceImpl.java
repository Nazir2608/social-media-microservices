package com.socialmedia.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtServiceImpl(@Value("${security.jwt.secret}") String secret, @Value("${security.jwt.expiration}") long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    @Override
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    @Override
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        Object username = claims.get("username");
        return username != null ? username.toString() : null;
    }

    @Override
    public boolean isTokenValid(String token, Long userId) {
        Long tokenUserId = extractUserId(token);
        Claims claims = extractAllClaims(token);
        Date expiration = claims.getExpiration();
        return tokenUserId.equals(userId) && expiration.after(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
