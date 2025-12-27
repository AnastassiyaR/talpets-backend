package com.backend.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    @Schema(description = "Comment identifier", example = "1")
    private Long id;

    @Schema(description = "Product identifier", example = "101")
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Schema(description = "Comment text content", example = "Great product!")
    @NotBlank(message = "Comment text cannot be empty")
    @Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters")
    private String commentText;

    @Schema(description = "Comment creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdDate;

    @Schema(description = "User identifier who created the comment", example = "5")
    private Long userId;
}
