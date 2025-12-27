package com.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data for signup")
public class UserSignupDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Schema(description = "Email", example = "ivan@gmail.com")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Schema(description = "Password", example = "123456")
    private String password;

    @Schema(description = "First name", example = "Ivan")
    private String firstName;

    @Schema(description = "Last name", example = "Smith")
    private String lastName;
}
