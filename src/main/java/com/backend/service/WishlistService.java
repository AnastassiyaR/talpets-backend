package com.backend.service;


import com.backend.dto.WishlistItemResponseDTO;
import com.backend.exception.ResourceAlreadyExistsException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.mapper.WishlistMapper;
import com.backend.model.Product;
import com.backend.model.Wishlist;
import com.backend.repository.ProductRepository;
import com.backend.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final WishlistMapper wishlistMapper;

    private static final String PRODUCT_NOT_FOUND = "Product not found";
    private static final String PRODUCT_ALREADY_IN_WISHLIST = "Product already in wishlist";


    @Transactional(readOnly = true)
    public List<WishlistItemResponseDTO> getUserWishlist(Long userId) {
        log.debug("Fetching wishlist for user: {}", userId);

        List<Wishlist> wishlistItems = wishlistRepository.findByUserId(userId);

        log.info("Found {} items in wishlist for user: {}", wishlistItems.size(), userId);

        return wishlistItems.stream()
                .map(wishlistMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public WishlistItemResponseDTO addToWishlist(Long userId, Long productId) {
        log.debug("Adding product {} to wishlist for user: {}", productId, userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", productId);
                    return new ResourceNotFoundException(PRODUCT_NOT_FOUND);
                });

        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            log.warn("Product {} already in wishlist for user: {}", productId, userId);
            throw new ResourceAlreadyExistsException(PRODUCT_ALREADY_IN_WISHLIST);
        }

        Wishlist wishlist = Wishlist.builder()
                .userId(userId)
                .productId(productId)
                .build();

        wishlist = wishlistRepository.save(wishlist);
        log.info("Successfully added product {} to wishlist for user: {}", productId, userId);

        return wishlistMapper.toResponseDTO(wishlist, product);
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        log.debug("Removing product {} from wishlist for user: {}", productId, userId);

        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            log.warn("Product {} not found in wishlist for user: {}", productId, userId);
            throw new ResourceNotFoundException("Product not in wishlist");
        }

        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        log.info("Removed product {} from wishlist for user: {}", productId, userId);
    }

    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long productId) {
        log.debug("Checking if product {} is in wishlist for user: {}", productId, userId);

        boolean exists = wishlistRepository.existsByUserIdAndProductId(userId, productId);

        log.debug("Product {} in wishlist for user {}: {}", productId, userId, exists);
        return exists;
    }

    @Transactional
    public void clearWishlist(Long userId) {
        log.debug("Clearing wishlist for user: {}", userId);


        wishlistRepository.deleteByUserId(userId);

        log.info("Cleared wishlist for user: {}", userId);
    }
}
