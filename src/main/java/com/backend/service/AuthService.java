package com.backend.service;

import com.backend.dto.UserLoginDTO;
import com.backend.dto.UserSignupDTO;
import com.backend.mapper.UserMapper;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserLoginDTO login(UserLoginDTO loginDTO) {
        Optional<User> userOpt = userRepository.findByEmail(loginDTO.getEmail());

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(loginDTO.getPassword())) {
            UserLoginDTO dto = userMapper.toLoginDto(userOpt.get());
            dto.setPassword(null);
            return dto;
        }
        throw new RuntimeException("Invalid email or password");
    }

    public UserSignupDTO signup(UserSignupDTO signupDTO) {
        User user = userMapper.toUserFromSignupDto(signupDTO);
        User savedUser = userRepository.save(user);
        return userMapper.toSignupDto(savedUser);
    }
}