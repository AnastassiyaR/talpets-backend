package com.backend.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for user feedback")
public class FeedbackDTO {

    @Schema(description = "Unique identifier of the feedback", example = "1")
    private Integer id;

    @Schema(description = "Identifier of the user who created the feedback", example = "1")
    private Long userId;

    @NotBlank(message = "Feedback text cannot be empty")
    @Schema(description = "Text content of the feedback", example = "The service was excellent!")
    private String feedbackText;

    @Schema(description = "Date and time when the feedback was created", example = "2025-11-13T22:10:00")
    private LocalDateTime createdAt;
}