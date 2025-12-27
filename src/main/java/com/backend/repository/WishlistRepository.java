package com.backend.repository;


import com.backend.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * Finds all wishlist items for a user with products eagerly loaded.
     *
     * Uses LEFT JOIN FETCH to load products in a single query, avoiding N+1 problem.
     *
     * Performance comparison:
     * - Without JOIN FETCH: 1 query for wishlist + N queries for products = N+1 queries
     * - With JOIN FETCH: 1 query total (all data loaded at once)
     *
     * @param userId the user ID
     * @return list of wishlist items with products loaded
     */
    @Query("SELECT w FROM Wishlist w LEFT JOIN FETCH w.product WHERE w.userId = :userId")
    List<Wishlist> findByUserId(@Param("userId") Long userId);

    @Modifying
    void deleteByUserId(Long userId);

    @Modifying
    void deleteByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
