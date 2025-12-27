package com.backend.controller;


import com.backend.dto.*;
import com.backend.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Account Management", description = "API for managing user account details")
@SecurityRequirement(name = "Bearer Authentication")
public class UserAccountController {

    private final UserAccountService userAccountService;
    private static final String MESSAGE = "message";


    @Operation(summary = "Change user's first name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "First name changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/change-firstname")
    public ResponseEntity<Map<String, String>> changeFirstName(@RequestBody FirstNameDTO dto) {
        String message = userAccountService.changeFirstName(dto);
        return ResponseEntity.ok(Map.of(MESSAGE, message));
    }


    @Operation(summary = "Change user's last name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Last name changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/change-lastname")
    public ResponseEntity<Map<String, String>> changeLastName(@RequestBody LastNameDTO dto) {
        String message = userAccountService.changeLastName(dto);
        return ResponseEntity.ok(Map.of(MESSAGE, message));
    }


    @Operation(summary = "Change user's email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already in use")
    })
    @PutMapping("/change-email")
    public ResponseEntity<Map<String, String>> changeEmail(@RequestBody ChangeEmailDTO dto) {
        String message = userAccountService.changeEmail(dto);
        return ResponseEntity.ok(Map.of(MESSAGE, message));
    }


    @Operation(summary = "Change user's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or weak password")
    })
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordDTO dto) {
        String message = userAccountService.changePassword(dto);
        return ResponseEntity.ok(Map.of(MESSAGE, message));
    }


    @Operation(summary = "Change user's profile photo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/change-photo")
    public ResponseEntity<Map<String, String>> changePhoto(@RequestBody UserPhotoDTO dto) {
        String message = userAccountService.changePhoto(dto);
        return ResponseEntity.ok(Map.of(MESSAGE, message));
    }
}
