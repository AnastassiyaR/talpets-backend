package com.backend.configuration;


import com.backend.model.User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token Provider - generates JWT tokens for authenticated users.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final SecretKey jwtKey;

    /**
     * Generates a signed JWT token containing:
     * - subject (email)
     * - firstName, lastName
     * - userId
     * - issuedAt and expiration (24h)
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("userId", user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .signWith(jwtKey)
                .compact();
    }
}
