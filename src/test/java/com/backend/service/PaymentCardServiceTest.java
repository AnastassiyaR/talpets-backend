package com.backend.service;

import com.backend.dto.PaymentCardRequestDTO;
import com.backend.dto.PaymentCardResponseDTO;
import com.backend.exception.InvalidCardException;
import com.backend.exception.ResourceAlreadyExistsException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.exception.UnauthorizedException;
import com.backend.mapper.PaymentCardMapper;
import com.backend.model.PaymentCard;
import com.backend.repository.PaymentCardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardService paymentCardService;

    // GET USER CARDS TESTS

    @Test
    void getUserCards_shouldReturnEmptyList_whenUserHasNoCards() {
        // GIVEN
        Long userId = 1L;
        given(paymentCardRepository.findByUserId(userId))
                .willReturn(List.of());

        // WHEN
        List<PaymentCardResponseDTO> result = paymentCardService.getUserCards(userId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
        then(paymentCardRepository).should().findByUserId(userId);
    }

    @Test
    void getUserCards_shouldReturnCardsList_whenUserHasCards() {
        // GIVEN
        Long userId = 1L;

        PaymentCard card1 = PaymentCard.builder()
                .id(1L)
                .userId(userId)
                .lastFourDigits("1234")
                .isDefault(true)
                .build();

        PaymentCard card2 = PaymentCard.builder()
                .id(2L)
                .userId(userId)
                .lastFourDigits("5678")
                .isDefault(false)
                .build();

        PaymentCardResponseDTO dto1 = PaymentCardResponseDTO.builder()
                .id(1L)
                .lastFourDigits("1234")
                .isDefault(true)
                .build();

        PaymentCardResponseDTO dto2 = PaymentCardResponseDTO.builder()
                .id(2L)
                .lastFourDigits("5678")
                .isDefault(false)
                .build();

        given(paymentCardRepository.findByUserId(userId))
                .willReturn(Arrays.asList(card1, card2));
        given(paymentCardMapper.toResponseDTO(card1)).willReturn(dto1);
        given(paymentCardMapper.toResponseDTO(card2)).willReturn(dto2);

        // WHEN
        List<PaymentCardResponseDTO> result = paymentCardService.getUserCards(userId);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1234", result.get(0).getLastFourDigits());
        assertEquals("5678", result.get(1).getLastFourDigits());
        assertTrue(result.get(0).isDefault());
        assertFalse(result.get(1).isDefault());

        then(paymentCardRepository).should().findByUserId(userId);
        then(paymentCardMapper).should().toResponseDTO(card1);
        then(paymentCardMapper).should().toResponseDTO(card2);
    }

    // ADD CARD TESTS

    @Test
    void addCard_shouldCreateCard_whenValidDataAndFirstCard() {
        // GIVEN
        Long userId = 1L;
        PaymentCardRequestDTO request = new PaymentCardRequestDTO();
        request.setCardNumber("4532015112830366"); // Valid test card (Luhn valid)
        request.setCardHolderName("John Doe");
        request.setExpiryMonth(12);
        request.setExpiryYear(2025);
        request.setCvv("123");
        request.setDefault(false);

        PaymentCard savedCard = PaymentCard.builder()
                .id(1L)
                .userId(userId)
                .cardNumber("****0366")
                .cardHolderName("John Doe")
                .expiryMonth(12)
                .expiryYear(2025)
                .lastFourDigits("0366")
                .isDefault(true) // Automatically set to true for first card
                .createdAt(LocalDateTime.now())
                .build();

        PaymentCardResponseDTO responseDTO = PaymentCardResponseDTO.builder()
                .id(1L)
                .lastFourDigits("0366")
                .isDefault(true)
                .build();

        given(paymentCardRepository.findByUserIdAndLastFourDigits(userId, "0366"))
                .willReturn(Optional.empty());
        given(paymentCardRepository.existsByUserId(userId))
                .willReturn(false); // First card
        given(paymentCardRepository.save(any(PaymentCard.class)))
                .willReturn(savedCard);
        given(paymentCardMapper.toResponseDTO(savedCard))
                .willReturn(responseDTO);

        // WHEN
        PaymentCardResponseDTO result = paymentCardService.addCard(userId, request);

        // THEN
        assertNotNull(result);
        assertEquals("0366", result.getLastFourDigits());
        assertTrue(result.isDefault());

        then(paymentCardRepository).should().findByUserIdAndLastFourDigits(userId, "0366");
        then(paymentCardRepository).should().existsByUserId(userId);
        then(paymentCardRepository).should().save(any(PaymentCard.class));
        then(paymentCardRepository).should(never()).resetDefaultCards(any());
    }

    @Test
    void addCard_shouldThrowException_whenCardNumberTooShort() {
        // GIVEN
        Long userId = 1L;
        PaymentCardRequestDTO request = new PaymentCardRequestDTO();
        request.setCardNumber("123456789"); // Too short
        request.setCardHolderName("John Doe");
        request.setExpiryMonth(12);
        request.setExpiryYear(2025);
        request.setCvv("123");

        // WHEN & THEN
        InvalidCardException exception = assertThrows(
                InvalidCardException.class,
                () -> paymentCardService.addCard(userId, request)
        );

        assertEquals("Card number must be 16 digits", exception.getMessage());
        then(paymentCardRepository).should(never()).save(any());
    }

    @Test
    void addCard_shouldThrowException_whenCardNumberHasLetters() {
        // GIVEN
        Long userId = 1L;
        PaymentCardRequestDTO request = new PaymentCardRequestDTO();
        request.setCardNumber("453201511283036A"); // Contains letter
        request.setCardHolderName("John Doe");
        request.setExpiryMonth(12);
        request.setExpiryYear(2025);
        request.setCvv("123");

        // WHEN & THEN
        InvalidCardException exception = assertThrows(
                InvalidCardException.class,
                () -> paymentCardService.addCard(userId, request)
        );

        assertEquals("Card number must contain only digits", exception.getMessage());
    }

    @Test
    void addCard_shouldThrowException_whenCardNumberFailsLuhnCheck() {
        // GIVEN
        Long userId = 1L;
        PaymentCardRequestDTO request = new PaymentCardRequestDTO();
        request.setCardNumber("4532015112830367"); // Invalid Luhn checksum
        request.setCardHolderName("John Doe");
        request.setExpiryMonth(12);
        request.setExpiryYear(2025);
        request.setCvv("123");

        // WHEN & THEN
        InvalidCardException exception = assertThrows(
                InvalidCardException.class,
                () -> paymentCardService.addCard(userId, request)
        );

        assertEquals("Invalid card number", exception.getMessage());
    }

    @Test
    void addCard_shouldThrowException_whenCardExpired() {
        // GIVEN
        Long userId = 1L;
        PaymentCardRequestDTO request = new PaymentCardRequestDTO();
        request.setCardNumber("4532015112830366");
        request.setCardHolderName("John Doe");
        request.setExpiryMonth(1);
        request.setExpiryYear(2020); // Expired
        request.setCvv("123");

        // WHEN & THEN
        InvalidCardException exception = assertThrows(
                InvalidCardException.class,
                () -> paymentCardService.addCard(userId, request)
        );

        assertEquals("Card has expired", exception.getMessage());
    }

    @Test
    void addCard_shouldThrowException_whenExpiryMonthInvalid() {
        // GIVEN
        Long userId = 1L;
        PaymentCardRequestDTO request = new PaymentCardRequestDTO();
        request.setCardNumber("4532015112830366");
        request.setCardHolderName("John Doe");
        request.setExpiryMonth(13); // Invalid month
        request.setExpiryYear(2025);
        request.setCvv("123");

        // WHEN & THEN
        InvalidCardException exception = assertThrows(
                InvalidCardException.class,
                () -> paymentCardService.addCard(userId, request)
        );

        assertEquals("Invalid expiry month", exception.getMessage());
    }

    @Test
    void addCard_shouldThrowException_whenCvvInvalid() {
        // GIVEN
        Long userId = 1L;
        PaymentCardRequestDTO request = new PaymentCardRequestDTO();
        request.setCardNumber("4532015112830366");
        request.setCardHolderName("John Doe");
        request.setExpiryMonth(12);
        request.setExpiryYear(2025);
        request.setCvv("12"); // Too short

        // WHEN & THEN
        InvalidCardException exception = assertThrows(
                InvalidCardException.class,
                () -> paymentCardService.addCard(userId, request)
        );

        assertEquals("CVV must be 3 or 4 digits", exception.getMessage());
    }

    @Test
    void addCard_shouldThrowException_whenCardAlreadyExists() {
        // GIVEN
        Long userId = 1L;
        PaymentCardRequestDTO request = new PaymentCardRequestDTO();
        request.setCardNumber("4532015112830366");
        request.setCardHolderName("John Doe");
        request.setExpiryMonth(12);
        request.setExpiryYear(2025);
        request.setCvv("123");

        PaymentCard existingCard = PaymentCard.builder()
                .id(1L)
                .userId(userId)
                .lastFourDigits("0366")
                .build();

        given(paymentCardRepository.findByUserIdAndLastFourDigits(userId, "0366"))
                .willReturn(Optional.of(existingCard));

        // WHEN & THEN
        ResourceAlreadyExistsException exception = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> paymentCardService.addCard(userId, request)
        );

        assertEquals("Card already exists", exception.getMessage());
        then(paymentCardRepository).should(never()).save(any());
    }

    // DELETE CARD TESTS

    @Test
    void deleteCard_shouldDeleteCard_whenCardExistsAndNotDefault() {
        // GIVEN
        Long userId = 1L;
        Long cardId = 1L;

        PaymentCard card = PaymentCard.builder()
                .id(cardId)
                .userId(userId)
                .lastFourDigits("1234")
                .isDefault(false)
                .build();

        given(paymentCardRepository.findById(cardId))
                .willReturn(Optional.of(card));

        // WHEN
        paymentCardService.deleteCard(userId, cardId);

        // THEN
        then(paymentCardRepository).should().findById(cardId);
        then(paymentCardRepository).should().delete(card);
        then(paymentCardRepository).should(never()).findByUserIdExcluding(any(), any());
    }

    @Test
    void deleteCard_shouldSetNewDefault_whenDeletingDefaultCard() {
        // GIVEN
        Long userId = 1L;
        Long cardId = 1L;

        PaymentCard defaultCard = PaymentCard.builder()
                .id(cardId)
                .userId(userId)
                .lastFourDigits("1234")
                .isDefault(true)
                .build();

        PaymentCard otherCard = PaymentCard.builder()
                .id(2L)
                .userId(userId)
                .lastFourDigits("5678")
                .isDefault(false)
                .build();

        given(paymentCardRepository.findById(cardId))
                .willReturn(Optional.of(defaultCard));
        given(paymentCardRepository.findByUserIdExcluding(userId, cardId))
                .willReturn(List.of(otherCard));

        // WHEN
        paymentCardService.deleteCard(userId, cardId);

        // THEN
        assertTrue(otherCard.isDefault());
        then(paymentCardRepository).should().findByUserIdExcluding(userId, cardId);
        then(paymentCardRepository).should().delete(defaultCard);
    }

    @Test
    void deleteCard_shouldThrowException_whenCardNotFound() {
        // GIVEN
        Long userId = 1L;
        Long cardId = 999L;

        given(paymentCardRepository.findById(cardId))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentCardService.deleteCard(userId, cardId)
        );

        assertEquals("Card not found", exception.getMessage());
        then(paymentCardRepository).should(never()).delete(any());
    }

    @Test
    void deleteCard_shouldThrowException_whenUserUnauthorized() {
        // GIVEN
        Long userId = 1L;
        Long cardId = 1L;
        Long otherUserId = 2L;

        PaymentCard card = PaymentCard.builder()
                .id(cardId)
                .userId(otherUserId) // Card belongs to different user
                .lastFourDigits("1234")
                .build();

        given(paymentCardRepository.findById(cardId))
                .willReturn(Optional.of(card));

        // WHEN & THEN
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> paymentCardService.deleteCard(userId, cardId)
        );

        assertEquals("Unauthorized to access this card", exception.getMessage());
        then(paymentCardRepository).should(never()).delete(any());
    }

    // SET DEFAULT CARD TESTS

    @Test
    void setDefaultCard_shouldSetCardAsDefault_whenValidRequest() {
        // GIVEN
        Long userId = 1L;
        Long cardId = 2L;

        PaymentCard card = PaymentCard.builder()
                .id(cardId)
                .userId(userId)
                .lastFourDigits("5678")
                .isDefault(false)
                .build();

        PaymentCard updatedCard = PaymentCard.builder()
                .id(cardId)
                .userId(userId)
                .lastFourDigits("5678")
                .isDefault(true)
                .build();

        PaymentCardResponseDTO responseDTO = PaymentCardResponseDTO.builder()
                .id(cardId)
                .lastFourDigits("5678")
                .isDefault(true)
                .build();

        given(paymentCardRepository.findById(cardId))
                .willReturn(Optional.of(card));
        given(paymentCardRepository.save(card))
                .willReturn(updatedCard);
        given(paymentCardMapper.toResponseDTO(updatedCard))
                .willReturn(responseDTO);

        // WHEN
        PaymentCardResponseDTO result = paymentCardService.setDefaultCard(userId, cardId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isDefault());
        assertEquals("5678", result.getLastFourDigits());

        then(paymentCardRepository).should().resetDefaultCards(userId);
        then(paymentCardRepository).should().save(card);
        then(paymentCardMapper).should().toResponseDTO(updatedCard);
    }

    @Test
    void setDefaultCard_shouldThrowException_whenCardNotFound() {
        // GIVEN
        Long userId = 1L;
        Long cardId = 999L;

        given(paymentCardRepository.findById(cardId))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentCardService.setDefaultCard(userId, cardId)
        );

        assertEquals("Card not found", exception.getMessage());
        then(paymentCardRepository).should(never()).resetDefaultCards(any());
        then(paymentCardRepository).should(never()).save(any());
    }

    @Test
    void setDefaultCard_shouldThrowException_whenUserUnauthorized() {
        // GIVEN
        Long userId = 1L;
        Long cardId = 1L;
        Long otherUserId = 2L;

        PaymentCard card = PaymentCard.builder()
                .id(cardId)
                .userId(otherUserId) // Card belongs to different user
                .lastFourDigits("1234")
                .build();

        given(paymentCardRepository.findById(cardId))
                .willReturn(Optional.of(card));

        // WHEN & THEN
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> paymentCardService.setDefaultCard(userId, cardId)
        );

        assertEquals("Unauthorized to access this card", exception.getMessage());
        then(paymentCardRepository).should(never()).resetDefaultCards(any());
        then(paymentCardRepository).should(never()).save(any());
    }
}
