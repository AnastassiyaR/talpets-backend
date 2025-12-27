package com.backend.controller;

import com.backend.AbstractIntegrationTest;
import com.backend.dto.*;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import com.backend.service.PhotoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Transactional
class UserAccountControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Сначала чистим всё
        photoService.cleanUploadFolder();
        userRepository.deleteAll();

        // Потом создаём тестового пользователя
        testUser = User.builder()
                .email("test@mail.com")
                .password(passwordEncoder.encode("oldPassword123"))
                .firstName("John")
                .lastName("Doe")
                .build();
        userRepository.save(testUser);
    }

    @Autowired
    private PhotoService photoService;

    // ==================== CHANGE FIRST NAME TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")  // 👈 ЗАЛОГИНЕН!
    void changeFirstName_shouldUpdateFirstName_whenDataIsValid() throws Exception {
        // GIVEN
        FirstNameDTO dto = new FirstNameDTO("NewJohn");

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-firstname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("First name updated successfully"));

        // Проверяем БД
        User updatedUser = userRepository.findByEmail("test@mail.com").orElseThrow();
        assertEquals("NewJohn", updatedUser.getFirstName());
    }

    @Test  // 👈 БЕЗ @WithMockUser - НЕ ЗАЛОГИНЕН!
    void changeFirstName_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // GIVEN
        FirstNameDTO dto = new FirstNameDTO("NewJohn");

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-firstname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isUnauthorized());  // 👈 401!
    }

    // ==================== CHANGE LAST NAME TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void changeLastName_shouldUpdateLastName_whenDataIsValid() throws Exception {
        // GIVEN
        LastNameDTO dto = new LastNameDTO("NewDoe");

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-lastname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Last name updated successfully"));

        // Проверяем БД
        User updatedUser = userRepository.findByEmail("test@mail.com").orElseThrow();
        assertEquals("NewDoe", updatedUser.getLastName());
    }

    @Test
    void changeLastName_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // GIVEN
        LastNameDTO dto = new LastNameDTO("NewDoe");

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-lastname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isUnauthorized());
    }

    // ==================== CHANGE EMAIL TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void changeEmail_shouldUpdateEmail_whenNewEmailIsUnique() throws Exception {
        // GIVEN
        ChangeEmailDTO dto = new ChangeEmailDTO("newemail@mail.com");

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email updated successfully. Please login again with new email."));

        // Проверяем БД
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("newemail@mail.com", updatedUser.getEmail());

        // Старый email больше не существует
        assertFalse(userRepository.findByEmail("test@mail.com").isPresent());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void changeEmail_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        // GIVEN - создаем другого пользователя
        User anotherUser = User.builder()
                .email("existing@mail.com")
                .password(passwordEncoder.encode("password"))
                .firstName("Jane")
                .lastName("Smith")
                .build();
        userRepository.save(anotherUser);

        ChangeEmailDTO dto = new ChangeEmailDTO("existing@mail.com");

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));

        // Проверяем что старый email НЕ изменился
        User unchangedUser = userRepository.findByEmail("test@mail.com").orElseThrow();
        assertEquals("test@mail.com", unchangedUser.getEmail());
    }

    @Test
    void changeEmail_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // GIVEN
        ChangeEmailDTO dto = new ChangeEmailDTO("newemail@mail.com");

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isUnauthorized());
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void changePassword_shouldUpdatePassword_whenDataIsValid() throws Exception {
        // GIVEN
        ChangePasswordDTO dto = new ChangePasswordDTO("newPassword456");

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));

        // Проверяем что пароль реально изменился
        User updatedUser = userRepository.findByEmail("test@mail.com").orElseThrow();
        assertTrue(passwordEncoder.matches("newPassword456", updatedUser.getPassword()));

        // Старый пароль больше не работает
        assertFalse(passwordEncoder.matches("oldPassword123", updatedUser.getPassword()));
    }

    @Test
    void changePassword_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // GIVEN
        ChangePasswordDTO dto = new ChangePasswordDTO("newPassword456");

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isUnauthorized());
    }

    // ==================== CHANGE PHOTO TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void changePhoto_shouldUpdatePhoto_whenDataIsValid() throws Exception {
        // GIVEN
        String base64Photo = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        UserPhotoDTO dto = new UserPhotoDTO(base64Photo);

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-photo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Photo updated successfully"));

        // Проверяем что фото сохранилось
        User updatedUser = userRepository.findByEmail("test@mail.com").orElseThrow();
        assertNotNull(updatedUser.getPhoto());
        assertTrue(updatedUser.getPhoto().endsWith(".png"));
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void changePhoto_shouldDeleteOldPhoto_whenUpdatingPhoto() throws Exception {
        // GIVEN - сначала устанавливаем фото
        testUser.setPhoto("old-photo.png");
        userRepository.save(testUser);

        String newBase64Photo = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        UserPhotoDTO dto = new UserPhotoDTO(newBase64Photo);

        // WHEN
        mockMvc.perform(put("/api/user/change-photo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isOk());

        // THEN - проверяем что фото изменилось
        User updatedUser = userRepository.findByEmail("test@mail.com").orElseThrow();
        assertNotEquals("old-photo.png", updatedUser.getPhoto());
    }

    @Test
    void changePhoto_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // GIVEN
        String base64Photo = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        UserPhotoDTO dto = new UserPhotoDTO(base64Photo);

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-photo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isUnauthorized());
    }

    // ==================== EDGE CASES ====================

    @Test
    @WithMockUser(username = "nonexistent@mail.com")  // 👈 Пользователя нет в базе!
    void changeFirstName_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        // GIVEN
        FirstNameDTO dto = new FirstNameDTO("NewName");

        // WHEN & THEN
        mockMvc.perform(put("/api/user/change-firstname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}