package com.backend.controller;

import com.backend.AbstractIntegrationTest;
import com.backend.dto.OrderRequestDTO;
import com.backend.model.*;
import com.backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private User user;
    private Product product;
    private PaymentCard paymentCard;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        paymentCardRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(
                User.builder()
                        .email("user@mail.com")
                        .password(passwordEncoder.encode("password123"))
                        .firstName("John")
                        .lastName("Doe")
                        .build()
        );

        product = productRepository.save(
                Product.builder()
                        .name("Dog Collar")
                        .price(BigDecimal.valueOf(20))
                        .size(SizeType.M)
                        .pet(PetType.DOG)
                        .color("Red")
                        .img("collar.png")
                        .build()
        );

        cartRepository.save(
                Cart.builder()
                        .userId(user.getId())
                        .productId(product.getId())
                        .quantity(2)
                        .selectedSize("M")
                        .build()
        );

        paymentCard = paymentCardRepository.save(
                PaymentCard.builder()
                        .userId(user.getId())
                        .cardNumber("4111111111111111")
                        .cardHolderName("John Doe")
                        .expiryMonth(12)
                        .expiryYear(2030)
                        .lastFourDigits("1111")
                        .isDefault(true)
                        .build()
        );

        token = obtainAccessToken("user@mail.com", "password123");
    }

    // CREATE ORDER
    @Test
    void createOrder_shouldCreateOrderAndClearCart() throws Exception {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .paymentCardId(paymentCard.getId())
                .build();

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber", startsWith("ORD-")))
                .andExpect(jsonPath("$.totalAmount").value(40))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paymentCardLastFour").value("1111"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName").value("Dog Collar"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(40));

        assertEquals(1, orderRepository.count());
        assertEquals(0, cartRepository.count());
    }

    // GET USER ORDERS
    @Test
    void getUserOrders_shouldReturnUserOrders() throws Exception {
        createOrder();

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].totalAmount").value(40));
    }

    // GET ORDER BY ID
    @Test
    void getOrderById_shouldReturnOrder() throws Exception {
        createOrder();
        Order order = orderRepository.findAll().get(0);

        mockMvc.perform(get("/api/orders/{id}", order.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.orderNumber").value(order.getOrderNumber()))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    // HELPER: create order once
    private void createOrder() throws Exception {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .paymentCardId(paymentCard.getId())
                .build();

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // AUTH HELPER
    private String obtainAccessToken(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }
}
