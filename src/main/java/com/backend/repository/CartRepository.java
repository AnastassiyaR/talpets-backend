package com.backend.repository;


import com.backend.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Retrieves all cart items for a specific user with products eagerly loaded.
     * Uses JOIN FETCH to avoid N+1 query problem.
     *
     * @param userId ID of the user
     * @return list of cart items with products
     */
    @Query("SELECT c FROM Cart c JOIN FETCH c.product WHERE c.userId = :userId")
    List<Cart> findByUserIdWithProducts(@Param("userId") Long userId);

    /**
     * Retrieves a single cart item by ID with product eagerly loaded.
     *
     * @param id cart item ID
     * @return Optional containing cart item with product if found
     */
    @Query("SELECT c FROM Cart c JOIN FETCH c.product WHERE c.id = :id")
    Optional<Cart> findByIdWithProduct(@Param("id") Long id);

    List<Cart> findByUserId(Long userId);

    Optional<Cart> findByUserIdAndProductIdAndSelectedSize(Long userId, Long productId, String selectedSize);

    @Modifying
    void deleteByUserId(Long userId);
}
