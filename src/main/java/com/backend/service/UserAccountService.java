package com.backend.service;

import com.backend.dto.FirstNameDTO;
import com.backend.dto.LastNameDTO;
import com.backend.dto.ChangeEmailDTO;
import com.backend.dto.ChangePasswordDTO;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return auth.getName();
    }


    public String changeFirstName(FirstNameDTO dto) {
        String currentEmail = getCurrentUserEmail();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(dto.getFirstName());

        userRepository.save(user);

        return "First name updated successfully";
    }


    public String changeLastName(LastNameDTO dto) {
        String currentEmail = getCurrentUserEmail();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastName(dto.getLastName());
        userRepository.save(user);

        return "Last name updated successfully";
    }


    public String changeEmail(ChangeEmailDTO dto) {
        String currentEmail = getCurrentUserEmail();

        if (userRepository.findByEmail(dto.getNewEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(dto.getNewEmail());
        userRepository.save(user);

        return "Email updated successfully. Please login again with new email.";
    }


    public String changePassword(ChangePasswordDTO dto) {
        String currentEmail = getCurrentUserEmail();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        return "Password updated successfully";
    }
}
