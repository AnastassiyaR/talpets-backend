package com.backend.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data for signup")
public class UserSignupDTO {

    @Schema(description = "Email", example = "ivan@gmail.con")
    private String email;

    @Schema(description = "Password", example = "123456")
    private String password;

    @Schema(description = "First name", example = "Ivan")
    private String firstName;

    @Schema(description = "Last name", example = "Smith")
    private String lastName;
}
