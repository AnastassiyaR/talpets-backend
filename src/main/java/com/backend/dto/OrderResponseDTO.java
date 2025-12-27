package com.backend.dto;


import com.backend.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order details with items")
public class OrderResponseDTO {

    @Schema(description = "Order identifier", example = "1")
    private Long id;

    @Schema(description = "Unique order number", example = "ORD-20241205-A3F2")
    private String orderNumber;

    @Schema(description = "Total order amount", example = "149.99")
    private BigDecimal totalAmount;

    @Schema(description = "Current order status", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "Last four digits of payment card", example = "4242")
    private String paymentCardLastFour;

    @Schema(description = "Order creation timestamp", example = "2024-12-05T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "List of ordered items")
    private List<OrderItemDTO> items;
}
