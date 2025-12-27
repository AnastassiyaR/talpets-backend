package com.backend.controller;

import com.backend.AbstractIntegrationTest;
import com.backend.dto.PaymentCardRequestDTO;
import com.backend.model.PaymentCard;
import com.backend.model.User;
import com.backend.repository.PaymentCardRepository;
import com.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PaymentCardController
 */
@Transactional
class PaymentCardControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder()
                .email("user@mail.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("John")
                .lastName("Doe")
                .build();
        user = userRepository.save(user);

        token = obtainAccessToken("user@mail.com", "password123");
    }

    // GET CARDS
    @Test
    void getUserCards_shouldReturnEmptyList_whenNoCards() throws Exception {
        mockMvc.perform(get("/api/payment-cards")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ADD CARD

    @Test
    void addCard_shouldCreateCardSuccessfully() throws Exception {
        PaymentCardRequestDTO request = validCardRequest(true);

        mockMvc.perform(post("/api/payment-cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cardHolderName").value("John Doe"))
                .andExpect(jsonPath("$.lastFourDigits").value("4242"))
                .andExpect(jsonPath("$.default").value(false));

        assertEquals(1, paymentCardRepository.count());
    }

    @Test
    void addCard_shouldReturnConflict_whenDuplicateCard() throws Exception {
        PaymentCardRequestDTO request = validCardRequest(true);

        mockMvc.perform(post("/api/payment-cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/payment-cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Card already exists"));
    }

    // DELETE CARD
    @Test
    void deleteCard_shouldRemoveCardSuccessfully() throws Exception {
        PaymentCard card = saveCard(true);

        mockMvc.perform(delete("/api/payment-cards/{id}", card.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertEquals(0, paymentCardRepository.count());
    }

    // SET DEFAULT CARD
    @Test
    void setDefaultCard_shouldUpdateDefaultCard() throws Exception {
        PaymentCard card = saveSecondCard();

        mockMvc.perform(put("/api/payment-cards/{id}/default", card.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(card.getId()))
                .andExpect(jsonPath("$.default").value(false));

        List<PaymentCard> cards = paymentCardRepository.findByUserId(user.getId());

        assertEquals(1,
                cards.stream().filter(PaymentCard::isDefault).count());

        assertEquals(card.getId(),
                cards.stream().filter(PaymentCard::isDefault)
                        .findFirst()
                        .orElseThrow()
                        .getId());
    }


    // HELPERS

    private PaymentCard saveCard(boolean isDefault) {
        return paymentCardRepository.save(
                PaymentCard.builder()
                        .userId(user.getId())
                        .cardNumber("****4242")
                        .cardHolderName("John Doe")
                        .expiryMonth(12)
                        .expiryYear(YearMonth.now().getYear() + 1)
                        .lastFourDigits("4242")
                        .isDefault(isDefault)
                        .build()
        );
    }

    private PaymentCard saveSecondCard() {
        return paymentCardRepository.save(
                PaymentCard.builder()
                        .userId(user.getId())
                        .cardNumber("****1111")
                        .cardHolderName("John Doe")
                        .expiryMonth(11)
                        .expiryYear(YearMonth.now().getYear() + 2)
                        .lastFourDigits("1111")
                        .isDefault(false)
                        .build()
        );
    }

    private PaymentCardRequestDTO validCardRequest(boolean isDefault) {
        return PaymentCardRequestDTO.builder()
                .cardNumber("4242424242424242") // валидный Luhn
                .cardHolderName("John Doe")
                .expiryMonth(12)
                .expiryYear(YearMonth.now().getYear() + 1)
                .cvv("123")
                .isDefault(isDefault)
                .build();
    }

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
