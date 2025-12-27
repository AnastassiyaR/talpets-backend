package com.backend.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object for shopping cart item")
public class CartDTO {

    @Schema(description = "Product identifier", example = "101")
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Schema(description = "Quantity of the product", example = "2")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Schema(description = "Selected size for the product", example = "M")
    private String selectedSize;
}
