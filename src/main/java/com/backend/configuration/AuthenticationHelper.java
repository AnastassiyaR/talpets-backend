package com.backend.configuration;


import com.backend.model.User;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Authentication Helper - securely extracts user information from JWT tokens.
 * It converts email from JWT (cryptographically signed) into userId for service layer operations.
 */
@Component
@RequiredArgsConstructor
public class AuthenticationHelper {

    private final UserRepository userRepository;

    /**
     * Securely extracts userId from JWT authentication.
     */
    public Long getUserId(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "User with email " + email + " not found"
                ));

        return user.getId();
    }
}
