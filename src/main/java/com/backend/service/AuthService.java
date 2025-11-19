package com.backend.service;

import com.backend.configuration.JwtUtil;
import com.backend.dto.LoginResponseDTO;
import com.backend.dto.SignupResponseDTO;
import com.backend.dto.UserLoginDTO;
import com.backend.dto.UserSignupDTO;
import com.backend.exception.InvalidCredentialsException;
import com.backend.exception.ResourceAlreadyExistsException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.mapper.UserMapper;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(UserLoginDTO loginDTO) {
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        String token = jwtUtil.generateToken(user);

        return new LoginResponseDTO(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    public SignupResponseDTO signup(UserSignupDTO signupDTO) {
        if (userRepository.findByEmail(signupDTO.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toUserFromSignupDto(signupDTO);
        user.setPassword(passwordEncoder.encode(signupDTO.getPassword()));

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser);

        return new SignupResponseDTO(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        );
    }
}
