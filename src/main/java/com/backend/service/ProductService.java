package com.backend.service;

import com.backend.dto.ProductResponseDTO;
import com.backend.exception.ResourceNotFoundException;
import com.backend.mapper.ProductMapper;
import com.backend.model.PetType;
import com.backend.model.Product;
import com.backend.model.SizeType;
import com.backend.repository.ProductRepository;
import com.backend.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductResponseDTO> findProducts(
            SizeType size,
            PetType pet,
            String color,
            String searchQuery
    ) {
        Specification<Product> spec = Specification.anyOf();

        if (size != null) {
            spec = spec.and(ProductSpecification.hasSize(size));
        }
        if (pet != null) {
            spec = spec.and(ProductSpecification.hasPet(pet));
        }
        if (color != null && !color.isBlank()) {
            spec = spec.and(ProductSpecification.hasColor(color));
        }
        if (searchQuery != null && !searchQuery.isBlank()) {
            spec = spec.and(ProductSpecification.nameContains(searchQuery));
        }

        List<Product> products = productRepository.findAll(spec);
        return productMapper.toFilterDtoList(products);
    }

    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product with id " + id + " not found"
                ));

        return productMapper.toFilterDto(product);
    }
}
