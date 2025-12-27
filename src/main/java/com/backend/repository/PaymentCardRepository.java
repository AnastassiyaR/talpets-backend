package com.backend.repository;


import com.backend.model.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

    List<PaymentCard> findByUserId(Long userId);

    Optional<PaymentCard> findByUserIdAndLastFourDigits(Long userId, String lastFourDigits);

    /**
     * Resets all cards to non-default for a specific user.
     * Used when setting a new default card.
     *
     * @param userId the user ID whose cards should be reset
     */
    @Modifying
    @Query("UPDATE PaymentCard c SET c.isDefault = false WHERE c.userId = :userId")
    void resetDefaultCards(@Param("userId") Long userId);

    /**
     * Finds all payment cards for a user excluding a specific card.
     * Used when deleting the default card to find a replacement.
     *
     * @param userId the user ID
     * @param excludeId the card ID to exclude
     * @return list of payment cards ordered by ID
     */
    @Query("SELECT c FROM PaymentCard c WHERE c.userId = :userId AND c.id != :excludeId ORDER BY c.id")
    List<PaymentCard> findByUserIdExcluding(@Param("userId") Long userId, @Param("excludeId") Long excludeId);

    /**
     * Checks if a user has any payment cards.
     * Used to determine if a new card should be automatically set as default.
     *
     * @param userId the user ID
     * @return true if user has at least one card, false otherwise
     */
    boolean existsByUserId(Long userId);
}
