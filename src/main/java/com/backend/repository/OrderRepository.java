package com.backend.repository;


import com.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Retrieves all orders for a specific user, ordered by creation date (newest first).
     * Uses LEFT JOIN FETCH to load order items in a single query and avoid N+1 issues.
     *
     * @param userId ID of the user whose orders should be retrieved
     * @return list of orders with their associated items
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items " +
            "WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Retrieves a single order by its ID along with all its items.
     * Uses LEFT JOIN FETCH to ensure items are loaded eagerly.
     *
     * @param orderId ID of the order to retrieve
     * @return Optional containing the order with its items if found
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}
