package com.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data for login")
public class UserLoginDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Schema(description = "Email", example = "ivan@gmail.com")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Schema(description = "Password", example = "123456")
    private String password;
}