package com.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Panel", description = "Access to admin-only endpoints using a secret header")
public class AdminController {

    @Value("${admin.secret}")
    private String adminSecret;

    @Operation(summary = "Access admin panel",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully entered admin panel"),
                    @ApiResponse(responseCode = "403", description = "Forbidden — wrong or missing secret")
            }
    )
    @GetMapping()
    public ResponseEntity<String> adminPage(@RequestParam String password) {
        if (password == null || !password.equals(adminSecret)) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Welcome to Admin Panel", HttpStatus.OK);
    }
}
