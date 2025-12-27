package com.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for changing user password")
public class ChangePasswordDTO {

    @Schema(description = "New password of the user", example = "123456")
    private String newPassword;

}
