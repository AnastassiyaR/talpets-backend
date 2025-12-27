package com.backend.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order item details")
public class OrderItemDTO {

    @Schema(description = "Order item identifier", example = "1")
    private Long id;

    @Schema(description = "Product identifier", example = "101")
    private Long productId;

    @Schema(description = "Product name at time of order", example = "T-Shirt")
    private String productName;

    @Schema(description = "Product image in base64 format")
    private String productImage;

    @Schema(description = "Product price at time of order", example = "29.99")
    private BigDecimal price;

    @Schema(description = "Quantity ordered", example = "2")
    private Integer quantity;

    @Schema(description = "Selected product size", example = "M")
    private String selectedSize;

    @Schema(description = "Subtotal for this item (price × quantity)", example = "59.98")
    private BigDecimal subtotal;
}
