package com.backend.controller;


import com.backend.configuration.AuthenticationHelper;
import com.backend.dto.WishlistItemResponseDTO;
import com.backend.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist Management", description = "API for managing user wishlist")
@SecurityRequirement(name = "Bearer Authentication")
public class WishlistController {

    private final WishlistService wishlistService;
    private final AuthenticationHelper authenticationHelper;

    @Operation(summary = "Get user's wishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist retrieved successfully (may be empty)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
    })
    @GetMapping
    public ResponseEntity<List<WishlistItemResponseDTO>> getWishlist(Authentication authentication) {
        Long userId = authenticationHelper.getUserId(authentication);
        List<WishlistItemResponseDTO> wishlist = wishlistService.getUserWishlist(userId);
        return ResponseEntity.ok(wishlist);
    }

    @Operation(summary = "Add product to wishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added to wishlist successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Product already in wishlist")
    })
    @PostMapping("/add/{productId}")
    public ResponseEntity<WishlistItemResponseDTO> addToWishlist(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        Long userId = authenticationHelper.getUserId(authentication);
        WishlistItemResponseDTO item = wishlistService.addToWishlist(userId, productId);
        return ResponseEntity.ok(item);
    }


    @Operation(summary = "Remove product from wishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product removed from wishlist successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        Long userId = authenticationHelper.getUserId(authentication);
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Check if product is in wishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> checkInWishlist(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        Long userId = authenticationHelper.getUserId(authentication);
        boolean inWishlist = wishlistService.isInWishlist(userId, productId);
        return ResponseEntity.ok(inWishlist);
    }


    @Operation(summary = "Clear entire wishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Wishlist cleared successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearWishlist(Authentication authentication) {
        Long userId = authenticationHelper.getUserId(authentication);
        wishlistService.clearWishlist(userId);
        return ResponseEntity.noContent().build();
    }
}
