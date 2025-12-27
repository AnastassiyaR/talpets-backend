package com.backend.configuration;


import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Provider of core security beans for the application.
 *
 * This class provides:
 * - PasswordEncoder for secure password hashing
 * - SecretKey for signing and verifying JWT tokens
 */
@Configuration
public class SecurityBeansProvider {

    /**
     * Provides password hashing using BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Creates a SecretKey for signing JWT tokens.
     */
    @Bean
    public SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
