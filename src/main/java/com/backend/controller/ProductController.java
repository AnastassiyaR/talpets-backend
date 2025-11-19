package com.backend.controller;

import com.backend.dto.ProductResponseDTO;
import com.backend.model.PetType;
import com.backend.model.SizeType;
import com.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "API for managing and retrieving products")
public class ProductController {

    private final ProductService productService;


    @Operation(summary = "Filter products by size, pet type, color, and search query")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of filtered products",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class)))
    })
    @GetMapping("/filter")
    public ResponseEntity<List<ProductResponseDTO>> filterProducts(
            @RequestParam(name = "productSize", required = false) SizeType size,
            @RequestParam(required = false) PetType pet,
            @RequestParam(required = false) String color,
            @RequestParam(value = "q", required = false) String searchQuery
    ) {
        List<ProductResponseDTO> products = productService.findProducts(
                size, pet, color, searchQuery
        );
        return ResponseEntity.ok(products);
    }


    @Operation(summary = "Get product by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
}
