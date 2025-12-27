package com.backend.controller;


import com.backend.configuration.AuthenticationHelper;
import com.backend.dto.PaymentCardRequestDTO;
import com.backend.dto.PaymentCardResponseDTO;
import com.backend.service.PaymentCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-cards")
@RequiredArgsConstructor
@Tag(name = "Payment Cards", description = "API for managing user payment cards")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;
    private final AuthenticationHelper authenticationHelper;


    @Operation(summary = "Get user's payment cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cards"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
    })
    @GetMapping
    public ResponseEntity<List<PaymentCardResponseDTO>> getUserCards(Authentication authentication) {
        Long userId = authenticationHelper.getUserId(authentication);
        List<PaymentCardResponseDTO> cards = paymentCardService.getUserCards(userId);
        return ResponseEntity.ok(cards);
    }


    @Operation(summary = "Add new payment card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid card data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<PaymentCardResponseDTO> addCard(
            @Valid @RequestBody PaymentCardRequestDTO request,
            Authentication authentication
    ) {
        Long userId = authenticationHelper.getUserId(authentication);
        PaymentCardResponseDTO card = paymentCardService.addCard(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }


    @Operation(summary = "Delete payment card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - card doesn't belong to user"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long cardId,
            Authentication authentication
    ) {
        Long userId = authenticationHelper.getUserId(authentication);
        paymentCardService.deleteCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Set card as default",
            description = "Sets the specified card as the default payment method. " +
                    "Previous default card will be unset.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Default card updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - card doesn't belong to user"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PutMapping("/{cardId}/default")
    public ResponseEntity<PaymentCardResponseDTO> setDefaultCard(
            @PathVariable Long cardId,
            Authentication authentication
    ) {
        Long userId = authenticationHelper.getUserId(authentication);
        PaymentCardResponseDTO card = paymentCardService.setDefaultCard(userId, cardId);
        return ResponseEntity.ok(card);
    }
}
