package com.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for changing user's first name")
public class FirstNameDTO {

    @Schema(description = "New first name of the user", example = "Ivan")
    private String firstName;
}
