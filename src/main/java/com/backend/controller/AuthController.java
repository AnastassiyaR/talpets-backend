package com.backend.controller;


import com.backend.dto.LoginResponseDTO;
import com.backend.dto.SignupResponseDTO;
import com.backend.dto.UserLoginDTO;
import com.backend.dto.UserSignupDTO;
import com.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "User login and signup")
public class AuthController {

    private final AuthService authService;


    @Operation(summary = "User login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Wrong data"),
            @ApiResponse(responseCode = "401", description = "Wrong email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        LoginResponseDTO response = authService.login(loginDTO);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "New user signup"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Data validation error"),
            @ApiResponse(responseCode = "409", description = "User with this email exists")
    })
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(@Valid @RequestBody UserSignupDTO signupDTO) {
        SignupResponseDTO response = authService.signup(signupDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
