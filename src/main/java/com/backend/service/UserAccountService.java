package com.backend.service;

import com.backend.dto.FirstNameDTO;
import com.backend.dto.LastNameDTO;
import com.backend.dto.ChangeEmailDTO;
import com.backend.dto.ChangePasswordDTO;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;

    public String changeFirstName(FirstNameDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(dto.getFirstName());
        userRepository.save(user);
        return "First name updated successfully";
    }

    public String changeLastName(LastNameDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastName(dto.getLastName());
        userRepository.save(user);
        return "Last name updated successfully";
    }

    public String changeEmail(ChangeEmailDTO dto) {
        User user = userRepository.findByEmail(dto.getCurrentEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(dto.getNewEmail());
        userRepository.save(user);
        return "Email updated successfully";
    }

    public String changePassword(ChangePasswordDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(dto.getNewPassword());
        userRepository.save(user);
        return "Password updated successfully";
    }
}
