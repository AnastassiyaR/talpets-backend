package com.backend.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cart item with complete product details")
public class CartItemResponseDTO {

    @Schema(description = "Cart item identifier", example = "1")
    private Long id;

    @Schema(description = "User identifier", example = "1")
    private Long userId;

    @Schema(description = "Product identifier", example = "101")
    private Long productId;

    @Schema(description = "Product name", example = "T-Shirt")
    private String productName;

    @Schema(description = "Product image in base64 format", example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    private String productImage;

    @Schema(description = "Product unit price", example = "29.99")
    private BigDecimal price;

    @Schema(description = "Quantity in cart", example = "2")
    private Integer quantity;

    @Schema(description = "Selected product size", example = "M")
    private String selectedSize;

    @Schema(description = "Total price (price × quantity)", example = "59.98")
    private BigDecimal totalPrice;
}
