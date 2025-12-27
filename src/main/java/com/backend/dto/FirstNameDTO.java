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
@Schema(description = "DTO for changing user's first name")
public class FirstNameDTO {

    @Schema(description = "New first name of the user", example = "Ivan")
    private String firstName;
}
