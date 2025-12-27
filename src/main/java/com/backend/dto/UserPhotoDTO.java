package com.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for changing user profile photo")
public class UserPhotoDTO {

    @Schema(description = "Profile photo in base64 format", example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    private String photo;
}
