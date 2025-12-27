package com.backend.controller;


import com.backend.configuration.AuthenticationHelper;
import com.backend.dto.OrderRequestDTO;
import com.backend.dto.OrderResponseDTO;
import com.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API for managing orders")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;
    private final AuthenticationHelper authenticationHelper;

    @Operation(summary = "Create new order from shopping cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Cart is empty or invalid payment card"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment card not found")
    })
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @RequestBody OrderRequestDTO request,
            Authentication authentication
    ) {
        Long userId = authenticationHelper.getUserId(authentication);
        OrderResponseDTO order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }


    @Operation(summary = "Get user's order history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(Authentication authentication) {
        Long userId = authenticationHelper.getUserId(authentication);
        List<OrderResponseDTO> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }


    @Operation(summary = "Get specific order details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not allowed to access this order"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        Long userId = authenticationHelper.getUserId(authentication);
        OrderResponseDTO order = orderService.getOrderById(userId, orderId);
        return ResponseEntity.ok(order);
    }
    
}
