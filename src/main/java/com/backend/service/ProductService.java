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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    private static final String PRODUCT_NOT_FOUND = "Product with id %d not found";


    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findProducts(
            List<SizeType> size,
            List<PetType> pet,
            List<String> color,
            String search
    ) {
        log.debug("Filtering products: size={}, pet={}, color={}, search={}", size, pet, color, search);

        Specification<Product> spec = Specification.anyOf();

        if (size != null && !size.isEmpty()) {
            spec = spec.and(ProductSpecification.hasSizes(size));
            log.debug("Added size filter: {}", size);
        }
        if (pet != null && !pet.isEmpty()) {
            spec = spec.and(ProductSpecification.hasPets(pet));
            log.debug("Added pet filter: {}", pet);
        }
        if (color != null && !color.isEmpty()) {
            spec = spec.and(ProductSpecification.hasColors(color));
            log.debug("Added color filter: {}", color);
        }

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and(ProductSpecification.nameContains(search));
            log.debug("Added search filter: {}", search);
        }

        List<Product> products = productRepository.findAll(spec);
        log.debug("Found {} products matching criteria", products.size());
        return productMapper.toFilterDtoList(products);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        log.debug("Fetching product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found: {}", id);
                    return new ResourceNotFoundException(String.format(PRODUCT_NOT_FOUND, id));
                });

        return productMapper.toFilterDto(product);
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductResponseDTO dto) {
        log.debug("Creating new product: {}", dto.getName());

        Product product = productMapper.toEntity(dto);
        Product savedProduct = productRepository.save(product);

        log.debug("Product created with id: {}", savedProduct.getId());
        return productMapper.toFilterDto(savedProduct);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductResponseDTO dto) {
        log.debug("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found for update: {}", id);
                    return new ResourceNotFoundException(String.format(PRODUCT_NOT_FOUND, id));
                });

        productMapper.updateProductFromDto(dto, product);

        log.debug("Product updated: {}", product.getId());

        return productMapper.toFilterDto(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.debug("Deleting product with id: {}", id);

        if (!productRepository.existsById(id)) {
            log.error("Product not found for deletion: {}", id);
            throw new ResourceNotFoundException(String.format(PRODUCT_NOT_FOUND, id));
        }

        productRepository.deleteById(id);
        log.debug("Product deleted: {}", id);
    }
}
