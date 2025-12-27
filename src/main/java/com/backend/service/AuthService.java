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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final PhotoService photoService;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(UserLoginDTO loginDTO) {
        log.debug("Attempting login for email: {}", loginDTO.getEmail());

        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with email: {}", loginDTO.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email: {}", loginDTO.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user);

        String photoData = user.getPhoto() != null ? photoService.getPhotoAsBase64(user.getPhoto()) : null;
        log.info("User logged in successfully: {}", user.getEmail());

        return LoginResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .photo(photoData)
                .build();
    }

    @Transactional
    public SignupResponseDTO signup(UserSignupDTO signupDTO) {
        log.debug("Attempting signup for email: {}", signupDTO.getEmail());

        if (userRepository.findByEmail(signupDTO.getEmail()).isPresent()) {
            log.warn("Signup failed: Email already exists: {}", signupDTO.getEmail());
            throw new ResourceAlreadyExistsException("User with email '" + signupDTO.getEmail() + "' already exists");
        }

        User user = userMapper.toUserFromSignupDto(signupDTO);
        user.setPassword(passwordEncoder.encode(signupDTO.getPassword()));

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser);

        log.info("User registered successfully: {}", savedUser.getEmail());

        return SignupResponseDTO.builder()
                .token(token)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .build();
    }
}
