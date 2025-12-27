package com.backend.controller;

import com.backend.AbstractIntegrationTest;
import com.backend.dto.UserLoginDTO;
import com.backend.dto.UserSignupDTO;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Transactional
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // LOGIN TESTS

    @Test
    void login_shouldReturnTokenAndUserData_whenCredentialsAreValid() throws Exception {
        User user = User.builder()
                .email("test@mail.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("John")
                .lastName("Doe")
                .build();
        User savedUser = userRepository.save(user);

        UserLoginDTO loginDTO = new UserLoginDTO("test@mail.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.email").value("test@mail.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.photo").isEmpty());
    }

    @Test
    void login_shouldReturnUnauthorized_whenUserNotFound() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO("notfound@mail.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_shouldReturnUnauthorized_whenPasswordIsWrong() throws Exception {
        User user = User.builder()
                .email("test@mail.com")
                .password(passwordEncoder.encode("correctPassword"))
                .firstName("John")
                .lastName("Doe")
                .build();
        userRepository.save(user);

        UserLoginDTO loginDTO = new UserLoginDTO("test@mail.com", "wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_shouldReturnBadRequest_whenEmailIsEmpty() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO("", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))

                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnBadRequest_whenPasswordIsEmpty() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO("test@mail.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))

                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO("invalid-email", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))

                .andExpect(status().isBadRequest());
    }

    // SIGNUP TESTS

    @Test
    void signup_shouldCreateUserInDatabase_whenDataIsValid() throws Exception {
        UserSignupDTO signupDTO = new UserSignupDTO(
                "newuser@mail.com",
                "password123",
                "Jane",
                "Smith"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDTO)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andExpect(jsonPath("$.userId").value(notNullValue()))
                .andExpect(jsonPath("$.email").value("newuser@mail.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        User savedUser = userRepository.findByEmail("newuser@mail.com")
                .orElseThrow(() -> new AssertionError("User not found in database"));

        assertEquals("Jane", savedUser.getFirstName());
        assertEquals("Smith", savedUser.getLastName());
        assertEquals("newuser@mail.com", savedUser.getEmail());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        assertNotNull(savedUser.getId());
    }

    @Test
    void signup_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        User existingUser = User.builder()
                .email("existing@mail.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("John")
                .lastName("Doe")
                .build();
        userRepository.save(existingUser);

        UserSignupDTO signupDTO = new UserSignupDTO(
                "existing@mail.com",
                "newpassword",
                "Jane",
                "Smith"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDTO)))

                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with email 'existing@mail.com' already exists"));

        long userCount = userRepository.count();
        assertEquals(1, userCount, "Should have only one user in database");
    }

    @Test
    void signup_shouldCreateUser_whenLastNameIsEmpty() throws Exception {
        UserSignupDTO signupDTO = new UserSignupDTO(
                "test@mail.com",
                "password123",
                "Jane",
                ""
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDTO)))

                .andExpect(status().isCreated());

        assertEquals(1, userRepository.count());
    }
}
