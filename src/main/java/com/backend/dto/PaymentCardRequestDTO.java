package com.backend.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for adding a new payment card")
public class PaymentCardRequestDTO {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^\\d{16}$", message = "Card number must be 16 digits")
    @Schema(description = "16-digit card number (digits only, no spaces)", example = "4532015112830366")
    private String cardNumber;

    @NotBlank(message = "Card holder name is required")
    @Schema(description = "Name of the card holder as it appears on the card", example = "John Smith")
    private String cardHolderName;

    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    @Schema(description = "Expiry month (1-12, where 1=January, 12=December)")
    private Integer expiryMonth;

    @NotNull(message = "Expiry year is required")
    @Min(value = 2024, message = "Year must be valid")
    @Schema(description = "Expiry year (4-digit year)", example = "2025")
    private Integer expiryYear;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^\\d{3,4}$", message = "CVV must be 3 or 4 digits")
    @Schema(description = "Card Verification Value (3-4 digits, never stored)", example = "123")
    private String cvv;

    @Builder.Default
    @Schema(description = "Whether to set this card as the default payment method",
            example = "false",
            defaultValue = "false")
    private boolean isDefault = false;
}
