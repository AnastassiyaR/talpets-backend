package com.backend.mapper;


import com.backend.dto.CartItemResponseDTO;
import com.backend.model.Cart;
import com.backend.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    /**
     * Converts Cart and Product entities to CartItemResponseDTO.
     * Maps cart information along with product details and calculates total price.
     *
     * @param cart the cart item entity
     * @param product the product entity associated with the cart item
     * @return CartItemResponseDTO with all cart and product information
     */
    @Mapping(target = "id", source = "cart.id")
    @Mapping(target = "userId", source = "cart.userId")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImage", source = "product.img")
    @Mapping(target = "price", source = "product.price")
    @Mapping(target = "quantity", source = "cart.quantity")
    @Mapping(target = "selectedSize", source = "cart.selectedSize")
    @Mapping(target = "totalPrice", expression = "java(calculateTotalPrice(cart, product))")
    CartItemResponseDTO toDto(Cart cart, Product product);

    /**
     * Calculates the total price for a cart item (price × quantity).
     * Returns zero if any required value is null.
     *
     * @param cart the cart item containing quantity
     * @param product the product containing unit price
     * @return total price as BigDecimal
     */
    @SuppressWarnings("unused")
    default BigDecimal calculateTotalPrice(Cart cart, Product product) {
        if (cart == null || product == null || product.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
    }
}
