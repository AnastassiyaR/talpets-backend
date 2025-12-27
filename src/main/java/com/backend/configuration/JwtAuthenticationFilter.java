package com.backend.configuration;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * JWT Authentication Filter - validates and processes JWT tokens for each request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey key;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            Optional<String> token = getToken(request);

            if (token.isPresent()) {
                Claims tokenBody = parseToken(token.get());

                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(buildAuthToken(tokenBody));
            }
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the Bearer token from Authorization header.
     */
    private Optional<String> getToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7));
    }

    /**
     * Parses and verifies the JWT token using the SecretKey.
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Builds the Spring Security Authentication object from token claims.
     */

    private Authentication buildAuthToken(Claims tokenBody) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                tokenBody.getSubject(),
                null,
                List.of(new SimpleGrantedAuthority(
                        String.valueOf(tokenBody.get("role"))))
        );

        auth.setDetails(tokenBody);

        return auth;
    }
}
