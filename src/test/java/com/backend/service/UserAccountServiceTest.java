package com.backend.service;

import com.backend.dto.*;
import com.backend.exception.ResourceAlreadyExistsException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PhotoService photoService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserAccountService userAccountService;

    private String currentUserEmail;
    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUserEmail = "test@mail.com";

        currentUser = User.builder()
                .id(1L)
                .email(currentUserEmail)
                .firstName("John")
                .lastName("Doe")
                .password("$2a$10$encodedPassword")
                .photo(null)
                .build();

        // Настройка SecurityContext для всех тестов
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getName()).willReturn(currentUserEmail);
    }

    // CHANGE FIRST NAME TESTS

    @Test
    void changeFirstName_shouldUpdateFirstNameSuccessfully() {
        // GIVEN
        FirstNameDTO dto = new FirstNameDTO("Ivan");
        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.of(currentUser));

        // WHEN
        String result = userAccountService.changeFirstName(dto);

        // THEN
        assertEquals("First name updated successfully", result);
        assertEquals("Ivan", currentUser.getFirstName());

        then(userRepository).should().findByEmail(currentUserEmail);
    }

    @Test
    void changeFirstName_shouldThrowResourceNotFoundException_whenUserNotFound() {
        // GIVEN
        FirstNameDTO dto = new FirstNameDTO("Ivan");
        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userAccountService.changeFirstName(dto)
        );

        assertEquals("User not found", exception.getMessage());

        then(userRepository).should().findByEmail(currentUserEmail);
    }

    // CHANGE LAST NAME TESTS

    @Test
    void changeLastName_shouldUpdateLastNameSuccessfully() {
        // GIVEN
        LastNameDTO dto = new LastNameDTO("Smith");
        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.of(currentUser));

        // WHEN
        String result = userAccountService.changeLastName(dto);

        // THEN
        assertEquals("Last name updated successfully", result);
        assertEquals("Smith", currentUser.getLastName());

        then(userRepository).should().findByEmail(currentUserEmail);
    }

    @Test
    void changeLastName_shouldThrowResourceNotFoundException_whenUserNotFound() {
        // GIVEN
        LastNameDTO dto = new LastNameDTO("Smith");
        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userAccountService.changeLastName(dto)
        );

        assertEquals("User not found", exception.getMessage());

        then(userRepository).should().findByEmail(currentUserEmail);
    }

    // CHANGE EMAIL TESTS

    @Test
    void changeEmail_shouldUpdateEmailSuccessfully() {
        // GIVEN
        String newEmail = "newemail@mail.com";
        ChangeEmailDTO dto = new ChangeEmailDTO(newEmail);

        given(userRepository.findByEmail(newEmail))
                .willReturn(Optional.empty());
        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.of(currentUser));

        // WHEN
        String result = userAccountService.changeEmail(dto);

        // THEN
        assertEquals("Email updated successfully. Please login again with new email.", result);
        assertEquals(newEmail, currentUser.getEmail());

        then(userRepository).should().findByEmail(newEmail);
        then(userRepository).should().findByEmail(currentUserEmail);
    }

    @Test
    void changeEmail_shouldThrowResourceAlreadyExistsException_whenEmailAlreadyExists() {
        // GIVEN
        String newEmail = "existing@mail.com";
        ChangeEmailDTO dto = new ChangeEmailDTO(newEmail);

        User existingUser = User.builder()
                .id(2L)
                .email(newEmail)
                .build();

        given(userRepository.findByEmail(newEmail))
                .willReturn(Optional.of(existingUser));

        // WHEN & THEN
        ResourceAlreadyExistsException exception = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> userAccountService.changeEmail(dto)
        );

        assertEquals("Email already exists", exception.getMessage());

        then(userRepository).should().findByEmail(newEmail);
        then(userRepository).should(never()).findByEmail(currentUserEmail);
    }

    @Test
    void changeEmail_shouldThrowResourceNotFoundException_whenUserNotFound() {
        // GIVEN
        String newEmail = "newemail@mail.com";
        ChangeEmailDTO dto = new ChangeEmailDTO(newEmail);

        given(userRepository.findByEmail(newEmail))
                .willReturn(Optional.empty());
        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userAccountService.changeEmail(dto)
        );

        assertEquals("User not found", exception.getMessage());

        then(userRepository).should().findByEmail(newEmail);
        then(userRepository).should().findByEmail(currentUserEmail);
    }

    // CHANGE PASSWORD TESTS

    @Test
    void changePassword_shouldUpdatePasswordSuccessfully() {
        // GIVEN
        String newPassword = "newPassword123";
        String encodedPassword = "$2a$10$newEncodedPassword";
        ChangePasswordDTO dto = new ChangePasswordDTO(newPassword);

        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.of(currentUser));
        given(passwordEncoder.encode(newPassword))
                .willReturn(encodedPassword);

        // WHEN
        String result = userAccountService.changePassword(dto);

        // THEN
        assertEquals("Password updated successfully", result);
        assertEquals(encodedPassword, currentUser.getPassword());

        then(userRepository).should().findByEmail(currentUserEmail);
        then(passwordEncoder).should().encode(newPassword);
    }

    @Test
    void changePassword_shouldThrowResourceNotFoundException_whenUserNotFound() {
        // GIVEN
        ChangePasswordDTO dto = new ChangePasswordDTO("newPassword123");
        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userAccountService.changePassword(dto)
        );

        assertEquals("User not found", exception.getMessage());

        then(userRepository).should().findByEmail(currentUserEmail);
        then(passwordEncoder).should(never()).encode(anyString());
    }

    // CHANGE PHOTO TESTS

    @Test
    void changePhoto_shouldUpdatePhotoSuccessfully_whenUserHasNoPhoto() {
        // GIVEN
        String base64Photo = "data:image/jpeg;base64,/9j/4AAQSkZJRg...";
        UserPhotoDTO dto = new UserPhotoDTO(base64Photo);
        String newPhotoFilename = "newphoto123.jpg";

        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.of(currentUser));
        given(photoService.savePhoto(base64Photo))
                .willReturn(newPhotoFilename);

        // WHEN
        String result = userAccountService.changePhoto(dto);

        // THEN
        assertEquals("Photo updated successfully", result);
        assertEquals(newPhotoFilename, currentUser.getPhoto());

        then(userRepository).should().findByEmail(currentUserEmail);
        then(photoService).should(never()).deletePhoto(anyString());
        then(photoService).should().savePhoto(base64Photo);
    }

    @Test
    void changePhoto_shouldUpdatePhotoSuccessfully_whenUserHasExistingPhoto() {
        // GIVEN
        String oldPhotoFilename = "oldphoto.jpg";
        String base64Photo = "data:image/jpeg;base64,/9j/4AAQSkZJRg...";
        String newPhotoFilename = "newphoto123.jpg";
        currentUser.setPhoto(oldPhotoFilename);

        UserPhotoDTO dto = new UserPhotoDTO(base64Photo);

        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.of(currentUser));
        doNothing().when(photoService).deletePhoto(oldPhotoFilename);
        given(photoService.savePhoto(base64Photo))
                .willReturn(newPhotoFilename);

        // WHEN
        String result = userAccountService.changePhoto(dto);

        // THEN
        assertEquals("Photo updated successfully", result);
        assertEquals(newPhotoFilename, currentUser.getPhoto());

        then(userRepository).should().findByEmail(currentUserEmail);
        then(photoService).should().deletePhoto(oldPhotoFilename);
        then(photoService).should().savePhoto(base64Photo);
    }

    @Test
    void changePhoto_shouldContinueWhenOldPhotoDeleteFails() {
        // GIVEN
        String oldPhotoFilename = "oldphoto.jpg";
        String base64Photo = "data:image/jpeg;base64,/9j/4AAQSkZJRg...";
        String newPhotoFilename = "newphoto123.jpg";
        currentUser.setPhoto(oldPhotoFilename);

        UserPhotoDTO dto = new UserPhotoDTO(base64Photo);

        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.of(currentUser));
        doThrow(new RuntimeException("Delete failed"))
                .when(photoService).deletePhoto(oldPhotoFilename);
        given(photoService.savePhoto(base64Photo))
                .willReturn(newPhotoFilename);

        // WHEN
        String result = userAccountService.changePhoto(dto);

        // THEN
        assertEquals("Photo updated successfully", result);
        assertEquals(newPhotoFilename, currentUser.getPhoto());

        then(userRepository).should().findByEmail(currentUserEmail);
        then(photoService).should().deletePhoto(oldPhotoFilename);
        then(photoService).should().savePhoto(base64Photo);
    }

    @Test
    void changePhoto_shouldThrowResourceNotFoundException_whenUserNotFound() {
        // GIVEN
        String base64Photo = "data:image/jpeg;base64,/9j/4AAQSkZJRg...";
        UserPhotoDTO dto = new UserPhotoDTO(base64Photo);

        given(userRepository.findByEmail(currentUserEmail))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userAccountService.changePhoto(dto)
        );

        assertEquals("User not found", exception.getMessage());

        then(userRepository).should().findByEmail(currentUserEmail);
        then(photoService).should(never()).deletePhoto(anyString());
        then(photoService).should(never()).savePhoto(anyString());
    }
}
