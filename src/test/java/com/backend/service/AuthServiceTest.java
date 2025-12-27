package com.backend.service;

import com.backend.configuration.JwtTokenProvider;
import com.backend.dto.LoginResponseDTO;
import com.backend.dto.SignupResponseDTO;
import com.backend.dto.UserLoginDTO;
import com.backend.dto.UserSignupDTO;
import com.backend.exception.InvalidCredentialsException;
import com.backend.exception.ResourceAlreadyExistsException;
import com.backend.mapper.UserMapper;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // 👇 MOCK - "полные притворщики" для зависимостей
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PhotoService photoService;

    // 👇 SPY - "частичный притворщик" (используем настоящий маппер)
    @Spy
    private UserMapper userMapper;

    // 👇 ТЕСТИРУЕМЫЙ СЕРВИС (в него вставляются моки)
    @InjectMocks
    private AuthService authService;

    // ==================== LOGIN TESTS ====================

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // GIVEN - подготовка тестовых данных и настройка моков
        UserLoginDTO loginDTO = new UserLoginDTO("test@mail.com", "password123");

        User user = User.builder()
                .id(1L)
                .email("test@mail.com")
                .password("$2a$10$encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .photo(null)
                .build();

        // Говорим мокам что они должны вернуть
        given(userRepository.findByEmail("test@mail.com"))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "$2a$10$encodedPassword"))
                .willReturn(true);
        given(jwtTokenProvider.generateToken(user))
                .willReturn("jwt-token-123");

        // WHEN - вызываем тестируемый метод
        LoginResponseDTO response = authService.login(loginDTO);

        // THEN - проверяем результат и взаимодействия с моками
        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("test@mail.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertNull(response.getPhoto());

        // Проверяем что моки были вызваны правильно
        then(userRepository).should().findByEmail("test@mail.com");
        then(passwordEncoder).should().matches("password123", "$2a$10$encodedPassword");
        then(jwtTokenProvider).should().generateToken(user);
        then(photoService).should(never()).getPhotoAsBase64(any());
    }

    @Test
    void login_shouldReturnTokenWithPhoto_whenUserHasPhoto() {
        // GIVEN
        UserLoginDTO loginDTO = new UserLoginDTO("test@mail.com", "password123");

        User user = User.builder()
                .id(1L)
                .email("test@mail.com")
                .password("$2a$10$encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .photo("photo123.jpg")
                .build();

        given(userRepository.findByEmail("test@mail.com"))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "$2a$10$encodedPassword"))
                .willReturn(true);
        given(jwtTokenProvider.generateToken(user))
                .willReturn("jwt-token-123");
        given(photoService.getPhotoAsBase64("photo123.jpg"))
                .willReturn("base64-encoded-photo-data");

        // WHEN
        LoginResponseDTO response = authService.login(loginDTO);

        // THEN
        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("test@mail.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("base64-encoded-photo-data", response.getPhoto());

        then(userRepository).should().findByEmail("test@mail.com");
        then(passwordEncoder).should().matches("password123", "$2a$10$encodedPassword");
        then(jwtTokenProvider).should().generateToken(user);
        then(photoService).should().getPhotoAsBase64("photo123.jpg");
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenUserNotFound() {
        // GIVEN
        UserLoginDTO loginDTO = new UserLoginDTO("notfound@mail.com", "password123");

        // Репозиторий вернет пустой Optional (пользователь не найден)
        given(userRepository.findByEmail("notfound@mail.com"))
                .willReturn(Optional.empty());

        // WHEN & THEN - проверяем что выброшено исключение
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginDTO)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        // Проверяем что другие моки НЕ были вызваны
        then(passwordEncoder).should(never()).matches(any(), any());
        then(jwtTokenProvider).should(never()).generateToken(any());
        then(photoService).should(never()).getPhotoAsBase64(any());
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenPasswordIsWrong() {
        // GIVEN
        UserLoginDTO loginDTO = new UserLoginDTO("test@mail.com", "wrongPassword");

        User user = User.builder()
                .id(1L)
                .email("test@mail.com")
                .password("$2a$10$encodedPassword")
                .build();

        given(userRepository.findByEmail("test@mail.com"))
                .willReturn(Optional.of(user));
        // Пароль не совпадает
        given(passwordEncoder.matches("wrongPassword", "$2a$10$encodedPassword"))
                .willReturn(false);

        // WHEN & THEN
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginDTO)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        then(userRepository).should().findByEmail("test@mail.com");
        then(passwordEncoder).should().matches("wrongPassword", "$2a$10$encodedPassword");
        then(jwtTokenProvider).should(never()).generateToken(any());
        then(photoService).should(never()).getPhotoAsBase64(any());
    }

    // ==================== SIGNUP TESTS ====================

    @Test
    void signup_shouldCreateUserAndReturnToken_whenEmailIsNew() {
        // GIVEN
        UserSignupDTO signupDTO = new UserSignupDTO(
                "newuser@mail.com",
                "password123",
                "Jane",
                "Smith"
        );

        User newUser = User.builder()
                .email("newuser@mail.com")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .email("newuser@mail.com")
                .password("$2a$10$encodedPassword")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        // Email не существует
        given(userRepository.findByEmail("newuser@mail.com"))
                .willReturn(Optional.empty());
        given(userMapper.toUserFromSignupDto(signupDTO))
                .willReturn(newUser);
        given(passwordEncoder.encode("password123"))
                .willReturn("$2a$10$encodedPassword");
        given(userRepository.save(any(User.class)))
                .willReturn(savedUser);
        given(jwtTokenProvider.generateToken(savedUser))
                .willReturn("jwt-token-456");

        // WHEN
        SignupResponseDTO response = authService.signup(signupDTO);

        // THEN
        assertNotNull(response);
        assertEquals("jwt-token-456", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("newuser@mail.com", response.getEmail());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());

        // Проверяем все взаимодействия с моками
        then(userRepository).should().findByEmail("newuser@mail.com");
        then(userMapper).should().toUserFromSignupDto(signupDTO);
        then(passwordEncoder).should().encode("password123");
        then(userRepository).should().save(any(User.class));
        then(jwtTokenProvider).should().generateToken(savedUser);
    }

    @Test
    void signup_shouldThrowResourceAlreadyExistsException_whenEmailAlreadyExists() {
        // GIVEN
        UserSignupDTO signupDTO = new UserSignupDTO(
                "existing@mail.com",
                "password123",
                "John",
                "Doe"
        );

        User existingUser = User.builder()
                .id(1L)
                .email("existing@mail.com")
                .build();

        // Email уже существует
        given(userRepository.findByEmail("existing@mail.com"))
                .willReturn(Optional.of(existingUser));

        // WHEN & THEN
        ResourceAlreadyExistsException exception = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> authService.signup(signupDTO)
        );

        assertEquals("User with email 'existing@mail.com' already exists", exception.getMessage());

        // Проверяем что сохранение НЕ произошло
        then(userRepository).should().findByEmail("existing@mail.com");
        then(userMapper).should(never()).toUserFromSignupDto(any());
        then(passwordEncoder).should(never()).encode(any());
        then(userRepository).should(never()).save(any());
        then(jwtTokenProvider).should(never()).generateToken(any());
    }
}
