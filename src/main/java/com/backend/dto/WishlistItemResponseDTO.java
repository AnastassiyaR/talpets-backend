package com.backend.dto;


import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Wishlist item with complete product details")
public class WishlistItemResponseDTO {

    @Schema(description = "Wishlist item ID", example = "1")
    private Long id;

    @Schema(description = "Product ID", example = "101")
    private Long productId;

    @Schema(description = "Product name", example = "Dog Collar")
    private String name;

    @Schema(description = "Product image", example = "collar.png")
    private String img;

    @Schema(description = "Product price", example = "15.99")
    private BigDecimal price;

    @Schema(description = "Product color", example = "Yellow")
    private String color;

    @Schema(description = "Product size", example = "M")
    private String size;
}
