package com.backend.service;


import com.backend.dto.OrderRequestDTO;
import com.backend.dto.OrderResponseDTO;
import com.backend.exception.EmptyCartException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.exception.UnauthorizedException;
import com.backend.mapper.OrderMapper;
import com.backend.model.*;
import com.backend.repository.CartRepository;
import com.backend.repository.OrderRepository;
import com.backend.repository.PaymentCardRepository;
import com.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final OrderMapper orderMapper;

    private static final String CART_EMPTY = "Cart is empty";
    private static final String PAYMENT_CARD_NOT_FOUND = "Payment card not found";
    private static final String PRODUCT_NOT_FOUND = "Product not found: ";
    private static final String ORDER_NOT_FOUND = "Order not found";
    private static final String UNAUTHORIZED = "Unauthorized to access this resource";

    @Transactional
    public OrderResponseDTO createOrder(Long userId, OrderRequestDTO request) {
        log.debug("Creating order for user: {}", userId);

        List<Cart> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            log.warn("Order creation failed - cart empty for user: {}", userId);
            throw new EmptyCartException(CART_EMPTY);
        }

        PaymentCard paymentCard = paymentCardRepository.findById(request.getPaymentCardId())
                .orElseThrow(() -> {
                    log.error("Payment card not found: {}", request.getPaymentCardId());
                    return new ResourceNotFoundException(PAYMENT_CARD_NOT_FOUND);
                });

        if (!paymentCard.getUserId().equals(userId)) {
            log.warn("Unauthorized payment card access: user {} tried to use card {}", userId, request.getPaymentCardId());
            throw new UnauthorizedException(UNAUTHORIZED);
        }

        // Extract product IDs from cart items
        List<Long> productIds = cartItems.stream()
                .map(Cart::getProductId)
                .toList();

        List<Product> products = productRepository.findAllById(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // Create the order entity with basic information
        Order order = Order.builder()
                .userId(userId)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .paymentCardLastFour(paymentCard.getLastFourDigits())
                .build();

        // Process each cart item and create order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cartItem : cartItems) {
            Product product = productMap.get(cartItem.getProductId());

            if (product == null) {
                log.error("Product not found during order creation: {}", cartItem.getProductId());
                throw new ResourceNotFoundException(PRODUCT_NOT_FOUND + cartItem.getProductId());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(cartItem.getProductId())
                    .productName(product.getName())
                    .productImage(product.getImg())
                    .price(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .selectedSize(cartItem.getSelectedSize())
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(orderItem);
        }

        // Set the total amount and save the order
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        // Clear the user's cart after successful order creation
        cartRepository.deleteByUserId(userId);
        log.info("Created order {} for user {}, cleared cart", order.getOrderNumber(), userId);

        return orderMapper.toResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getUserOrders(Long userId) {
        log.debug("Fetching orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(orderMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long userId, Long orderId) {
        log.debug("Fetching order {} for user {}", orderId, userId);
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found: {}", orderId);
                    return new ResourceNotFoundException(ORDER_NOT_FOUND);
                });

        if (!order.getUserId().equals(userId)) {
            log.warn("Unauthorized order access: user {} tried to access order {}", userId, orderId);
            throw new UnauthorizedException(UNAUTHORIZED);
        }

        return orderMapper.toResponseDTO(order);
    }

    /**
     * Generates a unique order number in the format: ORD-YYYYMMDD-XXXX
     * where YYYYMMDD is the current date and XXXX is a random alphanumeric string.
     *
     * Example: ORD-20241205-A3F2
     *
     * @return a unique order number string
     */
    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "ORD-" + date + "-" + random;
    }
}
