package com.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for changing user password")
public class ChangePasswordDTO {

    @Schema(description = "New password of the user", example = "123456")
    private String newPassword;
}
