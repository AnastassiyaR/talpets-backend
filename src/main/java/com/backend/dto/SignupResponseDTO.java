package com.backend.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Schema(description = "Response returned after successful signup")
public class SignupResponseDTO {

    @Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Unique identifier of the user", example = "1")
    private Long userId;

    @Schema(description = "Email of the user", example = "ivan@gmail.com")
    private String email;

    @Schema(description = "First name of the user", example = "Ivan")
    private String firstName;

    @Schema(description = "Last name of the user", example = "Smith")
    private String lastName;
}
