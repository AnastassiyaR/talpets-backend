package com.backend.mapper;


import com.backend.dto.WishlistItemResponseDTO;
import com.backend.model.Product;
import com.backend.model.Wishlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WishlistMapper {

    /**
     * Maps Wishlist entity to DTO when product is already loaded.
     *
     * @param wishlist the wishlist entity with product loaded
     * @return wishlist item DTO with product details
     */
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "img", source = "product.img")
    @Mapping(target = "price", source = "product.price")
    @Mapping(target = "color", source = "product.color")
    @Mapping(target = "size", source = "product.size", qualifiedByName = "sizeToString")
    WishlistItemResponseDTO toResponseDTO(Wishlist wishlist);

    /**
     * Maps Wishlist entity to DTO with separate Product parameter.
     * Used when product is fetched separately (e.g., in addToWishlist after validation).
     *
     * @param wishlist the wishlist entity
     * @param product the product entity (fetched separately)
     * @return wishlist item DTO with product details
     */
    @Mapping(target = "id", source = "wishlist.id")
    @Mapping(target = "productId", source = "wishlist.productId")
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "img", source = "product.img")
    @Mapping(target = "price", source = "product.price")
    @Mapping(target = "color", source = "product.color")
    @Mapping(target = "size", source = "product.size", qualifiedByName = "sizeToString")
    WishlistItemResponseDTO toResponseDTO(Wishlist wishlist, Product product);

    /**
     * Converts SizeType enum to String.
     *
     * @param size the size enum (can be null)
     * @return string representation of size, or null
     */
    @Named("sizeToString")
    default String sizeToString(Object size) {
        return size != null ? size.toString() : null;
    }
}
