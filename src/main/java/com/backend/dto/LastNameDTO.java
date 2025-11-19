package com.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for changing user's last name")
public class LastNameDTO {

    @Schema(description = "New last name of the user", example = "Smith")
    private String lastName;
}
