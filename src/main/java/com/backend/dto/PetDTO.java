package com.backend.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Pet profile information")
public class PetDTO {

    @Schema(description = "Unique pet identifier (generated automatically)", example = "1")
    private Long id;

    @NotBlank(message = "Pet name is required")
    @Schema(description = "Name of the pet", example = "Buddy")
    private String name;

    @Schema(description = "Breed of the pet", example = "Golden Retriever")
    private String breed;

    @Schema(description = "Gender of the pet", example = "Male", allowableValues = {"Male", "Female"})
    private String gender;

    @Past(message = "Birthday must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Birthday of the pet",
            example = "2020-05-15",
            format = "date")
    private LocalDate birthday;

    @Schema(description = "Age of the pet (e.g., '3 years old', '2 years 5 months')", example = "3 years old")
    private String age;

    @Schema(description = "Additional description or notes about the pet", example = "Friendly, loves playing fetch")
    private String description;

    private String photo;
}
