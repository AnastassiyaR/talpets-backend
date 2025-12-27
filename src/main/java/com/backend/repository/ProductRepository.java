package com.backend.repository;


import com.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {
    // JpaSpecificationExecutor provides:
    // - findAll(Specification<T> spec)
    // - findOne(Specification<T> spec)
    // - count(Specification<T> spec)
    // - exists(Specification<T> spec)
}
