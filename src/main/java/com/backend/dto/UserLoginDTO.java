package com.backend.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data for login")
public class UserLoginDTO {

    @Schema(description = "Email", example = "ivan@gmail.com")
    private String email;

    @Schema(description = "Password", example = "123456")
    private String password;
}
