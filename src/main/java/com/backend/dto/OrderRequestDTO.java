package com.backend.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create new order")
public class OrderRequestDTO {

    @Schema(description = "Payment card identifier", example = "1")
    @NotNull(message = "Payment card ID is required")
    private Long paymentCardId;
}
