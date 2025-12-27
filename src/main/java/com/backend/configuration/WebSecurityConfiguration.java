package com.backend.configuration;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Spring Security configuration for the application.
 */
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Main Spring Security configuration:
     * - Enables CORS
     * - Disables CSRF for REST APIs
     * - Configures stateless session (JWT-based)
     * - Secures endpoints
     * - Adds JWT filter before UsernamePasswordAuthenticationFilter
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/products/**").permitAll()
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/wishlist/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/feedback/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/feedback/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/feedback/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/feedback/**").authenticated()
                        .anyRequest().permitAll()
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");

                            Map<String, Object> body = new HashMap<>();
                            body.put("timestamp", LocalDateTime.now().toString());
                            body.put("status", 401);
                            body.put("error", "Unauthorized");
                            body.put("path", request.getRequestURI());

                            ObjectMapper mapper = new ObjectMapper();
                            mapper.writeValue(response.getOutputStream(), body);
                        })
                )


                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes AuthenticationManager needed for authentication.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
