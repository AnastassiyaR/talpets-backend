package com.backend.controller;


import com.backend.dto.ProductResponseDTO;
import com.backend.model.PetType;
import com.backend.model.SizeType;
import com.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            @ApiResponse(responseCode = "200", description = "List of filtered products (may be empty if no matches)")
    })
    @GetMapping("/filter")
    public ResponseEntity<List<ProductResponseDTO>> filterProducts(
            @RequestParam(required = false) List<SizeType> size,
            @RequestParam(required = false) List<PetType> pet,
            @RequestParam(required = false) List<String> color,
            @Parameter(description = "Search query for product name (case-insensitive partial match)")
            @RequestParam(required = false) String search
    ) {
        List<ProductResponseDTO> products = productService.findProducts(
                size, pet, color, search
        );
        return ResponseEntity.ok(products);
    }


    @Operation(summary = "Get product by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }


    @Operation(summary = "Create a new product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductResponseDTO productDTO
    ) {
        ProductResponseDTO created = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @Operation(summary = "Update an existing product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductResponseDTO productDTO
    ) {
        ProductResponseDTO updated = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updated);
    }


    @Operation(summary = "Delete a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
