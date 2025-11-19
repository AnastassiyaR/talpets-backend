package com.backend.specification;

import com.backend.model.PetType;
import com.backend.model.Product;
import com.backend.model.SizeType;
import org.springframework.data.jpa.domain.Specification;


public class ProductSpecification {

    public static Specification<Product> hasSize(SizeType size) {
        return (root, query, cb) -> cb.equal(root.get("size"), size);
    }

    public static Specification<Product> hasPet(PetType pet) {
        return (root, query, cb) -> cb.equal(root.get("pet"), pet);
    }

    public static Specification<Product> hasColor(String color) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("color")), color.toLowerCase());
    }

    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

}
