package com.backend.controller;


import com.backend.configuration.AuthenticationHelper;
import com.backend.dto.CartDTO;
import com.backend.dto.CartItemResponseDTO;
import com.backend.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "API for managing user shopping cart")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;
    private final AuthenticationHelper authenticationHelper;


    @Operation(summary = "Get user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<CartItemResponseDTO>> getCart(Authentication authentication) {
        Long userId = authenticationHelper.getUserId(authentication);
        List<CartItemResponseDTO> cart = cartService.getUserCart(userId);
        return ResponseEntity.ok(cart);
    }


    @Operation(summary = "Add product to cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping("/add")
    public ResponseEntity<CartItemResponseDTO> addToCart(
            @RequestBody CartDTO request,
            Authentication authentication
    ) {
        Long userId = authenticationHelper.getUserId(authentication);
        CartItemResponseDTO item = cartService.addToCart(
                userId,
                request.getProductId(),
                request.getQuantity(),
                request.getSelectedSize()
        );
        return ResponseEntity.ok(item);
    }


    @Operation(summary = "Update cart item quantity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PutMapping("/items/{cartId}")
    public ResponseEntity<CartItemResponseDTO> updateQuantity(
            @PathVariable Long cartId,
            @RequestParam Integer quantity,
            Authentication authentication
    ) {
        Long userId = authenticationHelper.getUserId(authentication);
        CartItemResponseDTO item = cartService.updateQuantity(userId, cartId, quantity);
        return ResponseEntity.ok(item);
    }


    @Operation(summary = "Remove item from cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @DeleteMapping("/items/{cartId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long cartId,
            Authentication authentication
    ) {
        Long userId = authenticationHelper.getUserId(authentication);
        cartService.removeFromCart(userId, cartId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Clear entire cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cart cleared successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        Long userId = authenticationHelper.getUserId(authentication);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Get cart total price")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total calculated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getCartTotal(Authentication authentication) {
        Long userId = authenticationHelper.getUserId(authentication);
        BigDecimal total = cartService.getCartTotal(userId);
        return ResponseEntity.ok(total);
    }
}
