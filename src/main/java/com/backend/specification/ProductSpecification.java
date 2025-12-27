package com.backend.specification;


import com.backend.model.PetType;
import com.backend.model.Product;
import com.backend.model.SizeType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Specification builder for Product entity.
 * Provides static methods to create type-safe, reusable query predicates.
 *
 * JPA Specifications allow building dynamic queries programmatically
 * without writing JPQL/SQL strings. Benefits:
 * - Type safety (compile-time checking)
 * - Reusability (combine specifications with AND/OR)
 * - Readability (declarative query building)
 */
public interface ProductSpecification {

    static Specification<Product> hasSizes(List<SizeType> sizes) {
        return (root, query, cb) -> root.get("size").in(sizes);
    }

    static Specification<Product> hasPets(List<PetType> pets) {
        return (root, query, cb) -> root.get("pet").in(pets);
    }

    static Specification<Product> hasColors(List<String> colors) {
        return (root, query, cb) -> root.get("color").in(colors);
    }

    static Specification<Product> nameContains(String searchQuery) {
        return (root, query, cb) -> {

            // Create LIKE pattern with wildcards
            String pattern = "%" + searchQuery.toLowerCase().trim() + "%";

            // Use LOWER() function for case-insensitive comparison
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }
}
