package com.backend.dto;

import com.backend.model.PetType;
import com.backend.model.SizeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for product")
public class ProductResponseDTO {

    @Schema(description = "Unique identifier of the product", example = "101")
    private Long id;

    @Schema(description = "Name of the product", example = "Dog Toy")
    private String name;

    @Schema(description = "Size of the product", example = "S")
    private SizeType size;

    @Schema(description = "Type of pet the product is for", example = "DOG")
    private PetType pet;

    @Schema(description = "Price of the product", example = "29.99")
    private BigDecimal price;

    @Schema(description = "URL of the product image", example = "https://example.com/images/dog-toy.jpg")
    private String img;
}
