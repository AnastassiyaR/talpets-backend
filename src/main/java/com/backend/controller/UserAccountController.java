package com.backend.controller;

import com.backend.dto.FirstNameDTO;
import com.backend.dto.LastNameDTO;
import com.backend.dto.ChangeEmailDTO;
import com.backend.dto.ChangePasswordDTO;
import com.backend.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserAccountController {

    private final UserAccountService userAccountService;

    @PutMapping("/change-firstname")
    public ResponseEntity<?> changeFirstName(@RequestBody FirstNameDTO dto) {
        try {
            return ResponseEntity.ok(userAccountService.changeFirstName(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-lastname")
    public ResponseEntity<?> changeLastName(@RequestBody LastNameDTO dto) {
        try {
            return ResponseEntity.ok(userAccountService.changeLastName(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-email")
    public ResponseEntity<?> changeEmail(@RequestBody ChangeEmailDTO dto) {
        try {
            return ResponseEntity.ok(userAccountService.changeEmail(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO dto) {
        try {
            return ResponseEntity.ok(userAccountService.changePassword(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
