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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final PaymentCardMapper paymentCardMapper;

    private static final String CARD_NOT_FOUND = "Card not found";
    private static final String CARD_ALREADY_EXISTS = "Card already exists";
    private static final String UNAUTHORIZED_ACCESS = "Unauthorized to access this card";

    @Transactional(readOnly = true)
    public List<PaymentCardResponseDTO> getUserCards(Long userId) {
        log.debug("Fetching payment cards for user: {}", userId);
        List<PaymentCard> cards = paymentCardRepository.findByUserId(userId);
        return cards.stream()
                .map(paymentCardMapper::toResponseDTO)
                .toList();
    }

    /**
     * Adds a new payment card for a user with comprehensive validation.
     * This method performs the following steps:
     * 1. Validates card number format and checksum (Luhn algorithm)
     * 2. Validates expiry date (not expired and not too far in future)
     * 3. Validates CVV format
     * 4. Checks for duplicate cards (same last 4 digits)
     * 5. Handles default card logic
     * 6. Encrypts and stores the card
     *
     * @param userId the ID of the user adding the card
     * @param request the payment card details from the request
     * @return the created payment card DTO
     * @throws InvalidCardException if card validation fails
     * @throws ResourceAlreadyExistsException if card already exists for this user
     */
    @Transactional
    public PaymentCardResponseDTO addCard(Long userId, PaymentCardRequestDTO request) {
        validateCardNumber(request.getCardNumber());
        validateExpiryDate(request.getExpiryMonth(), request.getExpiryYear());
        validateCVV(request.getCvv());

        String lastFourDigits = request.getCardNumber().substring(12);

        // Check if card with same last 4 digits already exists for this user
        paymentCardRepository.findByUserIdAndLastFourDigits(userId, lastFourDigits)
                .ifPresent(card -> {
                    log.warn("Duplicate card detected for user: {}", userId);
                    throw new ResourceAlreadyExistsException(CARD_ALREADY_EXISTS);
                });

        // Handle default card logic
        if (request.isDefault()) {
            log.debug("Setting new card as default, resetting other cards for user: {}", userId);
            paymentCardRepository.resetDefaultCards(userId);
        } else if (!paymentCardRepository.existsByUserId(userId)) {
            log.debug("First card for user: {}, automatically setting as default", userId);
            request.setDefault(true);
        }

        // Create and encrypt card entity
        PaymentCard card = PaymentCard.builder()
                .userId(userId)
                .cardNumber(encryptCardNumber(request.getCardNumber()))
                .cardHolderName(request.getCardHolderName())
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .lastFourDigits(lastFourDigits)
                .isDefault(request.isDefault())
                .build();

        card = paymentCardRepository.save(card);
        log.info("Successfully added payment card ending in {} for user: {}", lastFourDigits, userId);

        return paymentCardMapper.toResponseDTO(card);
    }

    @Transactional
    public void deleteCard(Long userId, Long cardId) {
        log.debug("Deleting card {} for user: {}", cardId, userId);

        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found: {}", cardId);
                    return new ResourceNotFoundException(CARD_NOT_FOUND);
                });

        if (!card.getUserId().equals(userId)) {
            log.warn("Unauthorized card deletion attempt: user {} tried to delete card {}", userId, cardId);
            throw new UnauthorizedException(UNAUTHORIZED_ACCESS);
        }

        if (card.isDefault()) {
            log.debug("Deleting default card, finding replacement for user: {}", userId);
            List<PaymentCard> otherCards = paymentCardRepository.findByUserIdExcluding(userId, cardId);

            if (!otherCards.isEmpty()) {
                // Set the first remaining card as default
                PaymentCard newDefaultCard = otherCards.getFirst();
                newDefaultCard.setDefault(true);
                log.info("Set card {} as new default after deletion", newDefaultCard.getId());
            }
        }

        paymentCardRepository.delete(card);
        log.info("Successfully deleted card {} for user: {}", cardId, userId);
    }

    @Transactional
    public PaymentCardResponseDTO setDefaultCard(Long userId, Long cardId) {
        log.debug("Setting card {} as default for user: {}", cardId, userId);

        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found: {}", cardId);
                    return new ResourceNotFoundException(CARD_NOT_FOUND);
                });

        if (!card.getUserId().equals(userId)) {
            log.warn("Unauthorized default card update: user {} tried to modify card {}", userId, cardId);
            throw new UnauthorizedException(UNAUTHORIZED_ACCESS);
        }

        paymentCardRepository.resetDefaultCards(userId);

        card.setDefault(true);
        card = paymentCardRepository.save(card);

        log.info("Successfully set card {} as default for user: {}", cardId, userId);
        return paymentCardMapper.toResponseDTO(card);
    }

    /**
     * Validates card number format and checksum.
     * Checks:
     * - Length is exactly 16 digits
     * - Contains only numeric characters
     * - Passes Luhn algorithm validation
     *
     * @param cardNumber the card number to validate
     * @throws InvalidCardException if validation fails
     */
    private void validateCardNumber(String cardNumber) {
        if (cardNumber.length() != 16) {
            throw new InvalidCardException("Card number must be 16 digits");
        }
        if (!cardNumber.matches("\\d+")) {
            throw new InvalidCardException("Card number must contain only digits");
        }
        if (!isValidCardNumberLuhn(cardNumber)) {
            throw new InvalidCardException("Invalid card number");
        }
    }

    /**
     * Validates card number using the Luhn algorithm (mod-10 checksum).
     * The Luhn algorithm is used by credit card companies to distinguish valid
     * card numbers from mistyped or otherwise incorrect numbers.
     *
     * Algorithm:
     * 1. Starting from the rightmost digit (excluding check digit), double every second digit
     * 2. If doubling results in a two-digit number, subtract 9
     * 3. Sum all digits
     * 4. If total modulo 10 equals 0, the number is valid
     *
     * @param cardNumber the card number to validate
     * @return true if card number passes Luhn check, false otherwise
     */
    private boolean isValidCardNumberLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9; // Equivalent to summing the two digits
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    /**
     * Validates card expiry date.
     * Checks:
     * - Month is between 1-12
     * - Card is not expired
     * - Expiry year is not more than 10 years in the future
     *
     * @param month the expiry month (1-12)
     * @param year the expiry year (full year, e.g., 2024)
     * @throws InvalidCardException if validation fails
     */
    private void validateExpiryDate(Integer month, Integer year) {
        if (month < 1 || month > 12) {
            throw new InvalidCardException("Invalid expiry month");
        }
        YearMonth cardExpiry = YearMonth.of(year, month);
        YearMonth currentYearMonth = YearMonth.now();
        if (cardExpiry.isBefore(currentYearMonth)) {
            throw new InvalidCardException("Card has expired");
        }
        if (year > LocalDate.now().getYear() + 10) {
            throw new InvalidCardException("Invalid expiry year");
        }
    }

    /**
     * Validates CVV format.
     * CVV must be 3 or 4 digits (3 for Visa/Mastercard, 4 for American Express).
     *
     * @param cvv the CVV to validate
     * @throws InvalidCardException if CVV format is invalid
     */
    private void validateCVV(String cvv) {
        if (!cvv.matches("\\d{3,4}")) {
            throw new InvalidCardException("CVV must be 3 or 4 digits");
        }
    }

    /**
     * "Encrypts" card number by masking all but the last 4 digits.
     *
     * @param cardNumber the full card number
     * @return masked card number (e.g., "****1234")
     */
    private String encryptCardNumber(String cardNumber) {
        return "****" + cardNumber.substring(12);
    }
}
