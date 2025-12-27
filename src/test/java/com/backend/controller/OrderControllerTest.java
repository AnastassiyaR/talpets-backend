package com.backend.controller;

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
import com.backend.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    // ==================== CREATE ORDER TESTS ====================

    @Test
    void createOrder_shouldCreateOrder_whenValidRequest() {
        // GIVEN
        Long userId = 1L;
        Long cardId = 10L;
        OrderRequestDTO request = new OrderRequestDTO();
        request.setPaymentCardId(cardId);

        // Cart items
        Cart cartItem1 = Cart.builder()
                .id(1L)
                .userId(userId)
                .productId(100L)
                .quantity(2)
                .selectedSize("M")
                .build();

        Cart cartItem2 = Cart.builder()
                .id(2L)
                .userId(userId)
                .productId(200L)
                .quantity(1)
                .selectedSize("L")
                .build();

        List<Cart> cartItems = Arrays.asList(cartItem1, cartItem2);

        // Payment card
        PaymentCard paymentCard = PaymentCard.builder()
                .id(cardId)
                .userId(userId)
                .lastFourDigits("4242")
                .build();

        // Products
        Product product1 = Product.builder()
                .id(100L)
                .name("T-Shirt")
                .price(new BigDecimal("29.99"))
                .img("tshirt.jpg")
                .build();

        Product product2 = Product.builder()
                .id(200L)
                .name("Jeans")
                .price(new BigDecimal("79.99"))
                .img("jeans.jpg")
                .build();

        List<Product> products = Arrays.asList(product1, product2);

        // Saved order
        Order savedOrder = Order.builder()
                .id(1L)
                .userId(userId)
                .orderNumber("ORD-20241205-A3F2")
                .totalAmount(new BigDecimal("139.97")) // 29.99*2 + 79.99
                .status(OrderStatus.PENDING)
                .paymentCardLastFour("4242")
                .createdAt(LocalDateTime.now())
                .items(new java.util.ArrayList<>())
                .build();

        OrderResponseDTO responseDTO = OrderResponseDTO.builder()
                .id(1L)
                .orderNumber("ORD-20241205-A3F2")
                .totalAmount(new BigDecimal("139.97"))
                .status(OrderStatus.PENDING)
                .build();

        given(cartRepository.findByUserId(userId)).willReturn(cartItems);
        given(paymentCardRepository.findById(cardId)).willReturn(Optional.of(paymentCard));
        given(productRepository.findAllById(Arrays.asList(100L, 200L))).willReturn(products);
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
        given(orderMapper.toResponseDTO(savedOrder)).willReturn(responseDTO);

        // WHEN
        OrderResponseDTO result = orderService.createOrder(userId, request);

        // THEN
        assertNotNull(result);
        assertEquals("ORD-20241205-A3F2", result.getOrderNumber());
        assertEquals(new BigDecimal("139.97"), result.getTotalAmount());
        assertEquals(OrderStatus.PENDING, result.getStatus());

        // Verify interactions
        then(cartRepository).should().findByUserId(userId);
        then(paymentCardRepository).should().findById(cardId);
        then(productRepository).should().findAllById(Arrays.asList(100L, 200L));
        then(orderRepository).should().save(any(Order.class));
        then(cartRepository).should().deleteByUserId(userId);
        then(orderMapper).should().toResponseDTO(savedOrder);
    }

    @Test
    void createOrder_shouldThrowException_whenCartIsEmpty() {
        // GIVEN
        Long userId = 1L;
        OrderRequestDTO request = new OrderRequestDTO();
        request.setPaymentCardId(10L);

        given(cartRepository.findByUserId(userId)).willReturn(List.of());

        // WHEN & THEN
        EmptyCartException exception = assertThrows(
                EmptyCartException.class,
                () -> orderService.createOrder(userId, request)
        );

        assertEquals("Cart is empty", exception.getMessage());

        // Verify no further processing occurred
        then(paymentCardRepository).should(never()).findById(any());
        then(productRepository).should(never()).findAllById(any());
        then(orderRepository).should(never()).save(any());
        then(cartRepository).should(never()).deleteByUserId(any());
    }

    @Test
    void createOrder_shouldThrowException_whenPaymentCardNotFound() {
        // GIVEN
        Long userId = 1L;
        Long cardId = 999L;
        OrderRequestDTO request = new OrderRequestDTO();
        request.setPaymentCardId(cardId);

        Cart cartItem = Cart.builder()
                .userId(userId)
                .productId(100L)
                .quantity(1)
                .build();

        given(cartRepository.findByUserId(userId)).willReturn(List.of(cartItem));
        given(paymentCardRepository.findById(cardId)).willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(userId, request)
        );

        assertEquals("Payment card not found", exception.getMessage());

        then(productRepository).should(never()).findAllById(any());
        then(orderRepository).should(never()).save(any());
        then(cartRepository).should(never()).deleteByUserId(any());
    }

    @Test
    void createOrder_shouldThrowException_whenPaymentCardBelongsToOtherUser() {
        // GIVEN
        Long userId = 1L;
        Long otherUserId = 2L;
        Long cardId = 10L;
        OrderRequestDTO request = new OrderRequestDTO();
        request.setPaymentCardId(cardId);

        Cart cartItem = Cart.builder()
                .userId(userId)
                .productId(100L)
                .quantity(1)
                .build();

        PaymentCard paymentCard = PaymentCard.builder()
                .id(cardId)
                .userId(otherUserId) // Card belongs to different user
                .lastFourDigits("4242")
                .build();

        given(cartRepository.findByUserId(userId)).willReturn(List.of(cartItem));
        given(paymentCardRepository.findById(cardId)).willReturn(Optional.of(paymentCard));

        // WHEN & THEN
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> orderService.createOrder(userId, request)
        );

        assertEquals("Unauthorized to access this resource", exception.getMessage());

        then(productRepository).should(never()).findAllById(any());
        then(orderRepository).should(never()).save(any());
        then(cartRepository).should(never()).deleteByUserId(any());
    }

    @Test
    void createOrder_shouldThrowException_whenProductNotFound() {
        // GIVEN
        Long userId = 1L;
        Long cardId = 10L;
        OrderRequestDTO request = new OrderRequestDTO();
        request.setPaymentCardId(cardId);

        Cart cartItem = Cart.builder()
                .userId(userId)
                .productId(999L) // Non-existent product
                .quantity(1)
                .build();

        PaymentCard paymentCard = PaymentCard.builder()
                .id(cardId)
                .userId(userId)
                .lastFourDigits("4242")
                .build();

        given(cartRepository.findByUserId(userId)).willReturn(List.of(cartItem));
        given(paymentCardRepository.findById(cardId)).willReturn(Optional.of(paymentCard));
        given(productRepository.findAllById(List.of(999L))).willReturn(List.of()); // Product not found

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(userId, request)
        );

        assertTrue(exception.getMessage().startsWith("Product not found:"));

        then(orderRepository).should(never()).save(any());
        then(cartRepository).should(never()).deleteByUserId(any());
    }

    @Test
    void createOrder_shouldClearCart_afterSuccessfulOrderCreation() {
        // GIVEN
        Long userId = 1L;
        Long cardId = 10L;
        OrderRequestDTO request = new OrderRequestDTO();
        request.setPaymentCardId(cardId);

        Cart cartItem = Cart.builder()
                .userId(userId)
                .productId(100L)
                .quantity(1)
                .selectedSize("M")
                .build();

        PaymentCard paymentCard = PaymentCard.builder()
                .id(cardId)
                .userId(userId)
                .lastFourDigits("4242")
                .build();

        Product product = Product.builder()
                .id(100L)
                .name("Item")
                .price(new BigDecimal("50.00"))
                .img("item.jpg")
                .build();

        Order savedOrder = Order.builder()
                .id(1L)
                .userId(userId)
                .orderNumber("ORD-20241205-TEST")
                .totalAmount(new BigDecimal("50.00"))
                .status(OrderStatus.PENDING)
                .items(new java.util.ArrayList<>())
                .build();

        given(cartRepository.findByUserId(userId)).willReturn(List.of(cartItem));
        given(paymentCardRepository.findById(cardId)).willReturn(Optional.of(paymentCard));
        given(productRepository.findAllById(List.of(100L))).willReturn(List.of(product));
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        // WHEN
        orderService.createOrder(userId, request);

        // THEN
        then(cartRepository).should().deleteByUserId(userId);
    }

    // ==================== GET USER ORDERS TESTS ====================

    @Test
    void getUserOrders_shouldReturnEmptyList_whenUserHasNoOrders() {
        // GIVEN
        Long userId = 1L;
        given(orderRepository.findByUserIdOrderByCreatedAtDesc(userId)).willReturn(List.of());

        // WHEN
        List<OrderResponseDTO> result = orderService.getUserOrders(userId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
        then(orderRepository).should().findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getUserOrders_shouldReturnOrdersList_whenUserHasOrders() {
        // GIVEN
        Long userId = 1L;

        Order order1 = Order.builder()
                .id(1L)
                .userId(userId)
                .orderNumber("ORD-20241205-ABC1")
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Order order2 = Order.builder()
                .id(2L)
                .userId(userId)
                .orderNumber("ORD-20241204-XYZ2")
                .totalAmount(new BigDecimal("200.00"))
                .status(OrderStatus.DELIVERED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        OrderResponseDTO dto1 = OrderResponseDTO.builder()
                .id(1L)
                .orderNumber("ORD-20241205-ABC1")
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .build();

        OrderResponseDTO dto2 = OrderResponseDTO.builder()
                .id(2L)
                .orderNumber("ORD-20241204-XYZ2")
                .totalAmount(new BigDecimal("200.00"))
                .status(OrderStatus.DELIVERED)
                .build();

        given(orderRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .willReturn(Arrays.asList(order1, order2));
        given(orderMapper.toResponseDTO(order1)).willReturn(dto1);
        given(orderMapper.toResponseDTO(order2)).willReturn(dto2);

        // WHEN
        List<OrderResponseDTO> result = orderService.getUserOrders(userId);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ORD-20241205-ABC1", result.get(0).getOrderNumber());
        assertEquals("ORD-20241204-XYZ2", result.get(1).getOrderNumber());

        then(orderRepository).should().findByUserIdOrderByCreatedAtDesc(userId);
        then(orderMapper).should().toResponseDTO(order1);
        then(orderMapper).should().toResponseDTO(order2);
    }

    // ==================== GET ORDER BY ID TESTS ====================

    @Test
    void getOrderById_shouldReturnOrder_whenOrderExistsAndUserAuthorized() {
        // GIVEN
        Long userId = 1L;
        Long orderId = 100L;

        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .orderNumber("ORD-20241205-TEST")
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.PENDING)
                .build();

        OrderResponseDTO responseDTO = OrderResponseDTO.builder()
                .id(orderId)
                .orderNumber("ORD-20241205-TEST")
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.PENDING)
                .build();

        given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(order));
        given(orderMapper.toResponseDTO(order)).willReturn(responseDTO);

        // WHEN
        OrderResponseDTO result = orderService.getOrderById(userId, orderId);

        // THEN
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("ORD-20241205-TEST", result.getOrderNumber());
        assertEquals(new BigDecimal("150.00"), result.getTotalAmount());

        then(orderRepository).should().findByIdWithItems(orderId);
        then(orderMapper).should().toResponseDTO(order);
    }

    @Test
    void getOrderById_shouldThrowException_whenOrderNotFound() {
        // GIVEN
        Long userId = 1L;
        Long orderId = 999L;

        given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(userId, orderId)
        );

        assertEquals("Order not found", exception.getMessage());
        then(orderMapper).should(never()).toResponseDTO(any());
    }

    @Test
    void getOrderById_shouldThrowException_whenUserUnauthorized() {
        // GIVEN
        Long userId = 1L;
        Long otherUserId = 2L;
        Long orderId = 100L;

        Order order = Order.builder()
                .id(orderId)
                .userId(otherUserId) // Order belongs to different user
                .orderNumber("ORD-20241205-TEST")
                .build();

        given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(order));

        // WHEN & THEN
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> orderService.getOrderById(userId, orderId)
        );

        assertEquals("Unauthorized to access this resource", exception.getMessage());
        then(orderMapper).should(never()).toResponseDTO(any());
    }
}
