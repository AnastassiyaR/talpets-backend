package com.backend.service;


import com.backend.dto.CartItemResponseDTO;
import com.backend.exception.ResourceNotFoundException;
import com.backend.exception.UnauthorizedException;
import com.backend.mapper.CartMapper;
import com.backend.model.Cart;
import com.backend.model.Product;
import com.backend.repository.CartRepository;
import com.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    private static final String PRODUCT_NOT_FOUND = "Product not found";
    private static final String PRODUCT_NOT_FOUND_LOG = "Product not found with id: {}";
    private static final String CART_ITEM_NOT_FOUND = "Cart item not found";


    @Transactional(readOnly = true)
    public List<CartItemResponseDTO> getUserCart(Long userId) {
        log.debug("Fetching cart for user: {}", userId);
        List<Cart> cartItems = cartRepository.findByUserIdWithProducts(userId);

        return cartItems.stream()
                .map(cart -> cartMapper.toDto(cart, cart.getProduct()))
                .toList();
    }

    @Transactional
    public CartItemResponseDTO addToCart(Long userId, Long productId, Integer quantity, String selectedSize) {
        log.debug("Adding product {} to cart for user: {}", productId, userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error(PRODUCT_NOT_FOUND_LOG, productId);
                    return new ResourceNotFoundException(PRODUCT_NOT_FOUND);
                });

        Optional<Cart> existingCart = cartRepository.findByUserIdAndProductIdAndSelectedSize(
                userId, productId, selectedSize
        );

        Cart cart;
        if (existingCart.isPresent()) {
            cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + quantity);
            log.info("Updated cart item for user {}, product {}", userId, productId);
        } else {
            cart = Cart.builder()
                    .userId(userId)
                    .productId(productId)
                    .quantity(quantity)
                    .selectedSize(selectedSize)
                    .build();
            log.info("Created new cart item for user {}, product {}", userId, productId);
        }

        cart = cartRepository.save(cart);
        return cartMapper.toDto(cart, product);
    }

    @Transactional
    public CartItemResponseDTO updateQuantity(Long userId, Long cartId, Integer quantity) {
        log.debug("Updating cart item {} quantity to {} for user: {}", cartId, quantity, userId);

        Cart cart = cartRepository.findByIdWithProduct(cartId)
                .orElseThrow(() -> {
                    log.error("Cart item not found with id: {}", cartId);
                    return new ResourceNotFoundException(CART_ITEM_NOT_FOUND);
                });

        validateCartOwnership(cart, userId, cartId);

        if (quantity <= 0) {
            log.info("Removing cart item {} for user {}", cartId, userId);
            cartRepository.delete(cart);
            return null;
        }

        cart.setQuantity(quantity);
        cart = cartRepository.save(cart);
        log.info("Updated cart item {} quantity to {} for user {}", cartId, quantity, userId);

        return cartMapper.toDto(cart, cart.getProduct());
    }

    @Transactional
    public void removeFromCart(Long userId, Long cartId) {
        log.debug("Removing cart item {} for user: {}", cartId, userId);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    log.error("Cart item not found with id: {}", cartId);
                    return new ResourceNotFoundException(CART_ITEM_NOT_FOUND);
                });

        validateCartOwnership(cart, userId, cartId);

        cartRepository.delete(cart);
        log.info("Removed cart item {} for user {}", cartId, userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        log.debug("Clearing cart for user: {}", userId);
        cartRepository.deleteByUserId(userId);
        log.info("Cleared cart for user: {}", userId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(Long userId) {
        log.debug("Calculating cart total for user: {}", userId);

        List<Cart> cartItems = cartRepository.findByUserIdWithProducts(userId);

        BigDecimal total = cartItems.stream()
                .map(cart -> cart.getProduct().getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Cart total for user {}: {}", userId, total);
        return total;
    }

    /**
     * Validates that the cart item belongs to the specified user.
     * Throws UnauthorizedException if ownership check fails.
     */
    private void validateCartOwnership(Cart cart, Long userId, Long cartId) {
        if (!cart.getUserId().equals(userId)) {
            log.warn("Unauthorized access attempt: user {} tried to access cart item {}", userId, cartId);
            throw new UnauthorizedException("You are not allowed to access this cart item");
        }
    }
}
