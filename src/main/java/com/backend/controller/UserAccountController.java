package com.backend.controller;

import com.backend.dto.FirstNameDTO;
import com.backend.dto.LastNameDTO;
import com.backend.dto.ChangeEmailDTO;
import com.backend.dto.ChangePasswordDTO;
import com.backend.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Account Management", description = "API for managing user account details")
public class UserAccountController {

    private final UserAccountService userAccountService;

    private static final String MESSAGE = "message";


    @Operation(summary = "Change user's first name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "First name changed successfully",
                    content = @Content(schema = @Schema(implementation = FirstNameDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/change-firstname")
    public ResponseEntity<Map<String, String>> changeFirstName(@RequestBody FirstNameDTO dto) {
        String message = userAccountService.changeFirstName(dto);
        return ResponseEntity.ok(Map.of(MESSAGE, message));
    }


    @Operation(summary = "Change user's last name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Last name changed successfully",
                    content = @Content(schema = @Schema(implementation = LastNameDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/change-lastname")
    public ResponseEntity<Map<String, String>> changeLastName(@RequestBody LastNameDTO dto) {
        String message = userAccountService.changeLastName(dto);
        return ResponseEntity.ok(Map.of(MESSAGE, message));
    }


    @Operation(summary = "Change user's email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email changed successfully",
                    content = @Content(schema = @Schema(implementation = ChangeEmailDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already in use")
    })
    @PutMapping("/change-email")
    public ResponseEntity<Map<String, String>> changeEmail(@RequestBody ChangeEmailDTO dto) {
        String message = userAccountService.changeEmail(dto);
        return ResponseEntity.ok(Map.of(MESSAGE, message));
    }


    @Operation(summary = "Change user's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully",
                    content = @Content(schema = @Schema(implementation = ChangePasswordDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or weak password")
    })
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordDTO dto) {
        String message = userAccountService.changePassword(dto);
        return ResponseEntity.ok(Map.of(MESSAGE, message));
    }
}
