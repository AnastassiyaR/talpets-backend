package com.backend.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;

    @Column(name = "last_four_digits", nullable = false)
    private String lastFourDigits;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically sets creation timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Lazy-loaded relationship to the user who owns this card.
     * Uses the userId column (not managed by JPA for performance).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
