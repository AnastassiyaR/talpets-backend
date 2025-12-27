package com.backend.service;


import com.backend.dto.*;
import com.backend.exception.ResourceAlreadyExistsException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhotoService photoService;

    private static final String USER_NOT_FOUND = "User not found";
    private static final String USER_NOT_FOUND_LOG = "User not found with email: {}";


    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            log.error("No authenticated user found in security context");
            throw new ResourceNotFoundException("No authenticated user");
        }
        return auth.getName();
    }

    @Transactional
    public String changeFirstName(FirstNameDTO dto) {
        String currentEmail = getCurrentUserEmail();
        log.debug("Changing first name for user: {}", currentEmail);

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG, currentEmail);
                    return new ResourceNotFoundException(USER_NOT_FOUND);
                });

        user.setFirstName(dto.getFirstName());

        log.info("First name updated successfully for user: {}", currentEmail);
        return "First name updated successfully";
    }

    @Transactional
    public String changeLastName(LastNameDTO dto) {
        String currentEmail = getCurrentUserEmail();
        log.debug("Changing last name for user: {}", currentEmail);

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG, currentEmail);
                    return new ResourceNotFoundException(USER_NOT_FOUND);
                });

        user.setLastName(dto.getLastName());

        log.info("Last name updated successfully for user: {}", currentEmail);
        return "Last name updated successfully";
    }

    @Transactional
    public String changeEmail(ChangeEmailDTO dto) {
        String currentEmail = getCurrentUserEmail();
        log.debug("Changing email for user: {} to new email: {}", currentEmail, dto.getNewEmail());

        if (userRepository.findByEmail(dto.getNewEmail()).isPresent()) {
            log.warn("Email change failed: email already exists: {}", dto.getNewEmail());
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG, currentEmail);
                    return new ResourceNotFoundException(USER_NOT_FOUND);
                });

        user.setEmail(dto.getNewEmail());

        log.info("Email updated successfully for user: {} to {}", currentEmail, dto.getNewEmail());
        return "Email updated successfully. Please login again with new email.";
    }

    @Transactional
    public String changePassword(ChangePasswordDTO dto) {
        String currentEmail = getCurrentUserEmail();
        log.debug("Changing password for user: {}", currentEmail);

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG, currentEmail);
                    return new ResourceNotFoundException(USER_NOT_FOUND);
                });

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        log.info("Password updated successfully for user: {}", currentEmail);
        return "Password updated successfully";
    }

    @Transactional
    public String changePhoto(UserPhotoDTO dto) {
        String currentEmail = getCurrentUserEmail();
        log.debug("Changing photo for user: {}", currentEmail);

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG, currentEmail);
                    return new ResourceNotFoundException(USER_NOT_FOUND);
                });

        String oldPhoto = user.getPhoto();
        if (oldPhoto != null) {
            try {
                photoService.deletePhoto(oldPhoto);
            } catch (Exception e) {
                log.warn("Failed to delete old photo: {} for user: {}", oldPhoto, currentEmail, e);
            }
        }

        String filename = photoService.savePhoto(dto.getPhoto());
        user.setPhoto(filename);

        log.info("Photo updated successfully for user: {}", currentEmail);
        return "Photo updated successfully";
    }
}
