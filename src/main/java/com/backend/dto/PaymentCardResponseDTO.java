package com.backend.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing masked payment card information")
public class PaymentCardResponseDTO {

    @Schema(description = "Unique card identifier", example = "1")
    private Long id;

    @Schema(description = "Name of the card holder", example = "John Smith")
    private String cardHolderName;

    @Schema(description = "Last 4 digits of the card", example = "1234")
    private String lastFourDigits;

    @Schema(description = "Fully masked card number", example = "**** **** **** 1234")
    private String maskedCardNumber;

    @Schema(description = "Expiry month (1-12)", example = "12")
    private Integer expiryMonth;

    /**
     * Card expiry year (4-digit year)
     */
    @Schema(description = "Expiry year", example = "2025")
    private Integer expiryYear;

    /**
     * Indicates if this is the user's default payment method
     */
    @Schema(description = "Whether this is the default payment card", example = "true")
    private boolean isDefault;

    /**
     * Timestamp when the card was added to the system
     */
    @Schema(description = "Card creation timestamp", example = "2024-12-05T10:30:00")
    private LocalDateTime createdAt;
}
