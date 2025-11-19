package com.backend.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for changing user email")
public class ChangeEmailDTO {

    @Schema(description = "New email address of the user", example = "ivan@example.com")
    private String newEmail;
}
