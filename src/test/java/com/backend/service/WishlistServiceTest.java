package com.backend.service;

import com.backend.dto.WishlistItemResponseDTO;
import com.backend.exception.ResourceAlreadyExistsException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.mapper.WishlistMapper;
import com.backend.model.PetType;
import com.backend.model.Product;
import com.backend.model.SizeType;
import com.backend.model.Wishlist;
import com.backend.repository.ProductRepository;
import com.backend.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WishlistMapper wishlistMapper;

    @InjectMocks
    private WishlistService wishlistService;

    private Long userId;
    private Long productId;
    private Product product;
    private Wishlist wishlist;
    private WishlistItemResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        userId = 1L;
        productId = 100L;

        product = Product.builder()
                .id(productId)
                .name("Dog Collar")
                .size(SizeType.M)
                .pet(PetType.DOG)
                .price(new BigDecimal("15.99"))
                .img("collar.png")
                .color("Yellow")
                .build();

        wishlist = Wishlist.builder()
                .id(1L)
                .userId(userId)
                .productId(productId)
                .build();

        responseDTO = WishlistItemResponseDTO.builder()
                .id(1L)
                .productId(productId)
                .name("Dog Collar")
                .img("collar.png")
                .price(new BigDecimal("15.99"))
                .color("Yellow")
                .size("M")
                .build();
    }

    @Test
    void getUserWishlist_ShouldReturnListOfWishlistItems() {
        // Arrange
        List<Wishlist> wishlistItems = Arrays.asList(wishlist);
        when(wishlistRepository.findByUserId(userId)).thenReturn(wishlistItems);
        when(wishlistMapper.toResponseDTO(any(Wishlist.class))).thenReturn(responseDTO);

        // Act
        List<WishlistItemResponseDTO> result = wishlistService.getUserWishlist(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseDTO, result.get(0));
        assertEquals("Dog Collar", result.get(0).getName());
        assertEquals(new BigDecimal("15.99"), result.get(0).getPrice());
        assertEquals("Yellow", result.get(0).getColor());
        assertEquals("M", result.get(0).getSize());

        verify(wishlistRepository, times(1)).findByUserId(userId);
        verify(wishlistMapper, times(1)).toResponseDTO(wishlist);
    }

    @Test
    void getUserWishlist_ShouldReturnEmptyList_WhenNoItemsFound() {
        // Arrange
        when(wishlistRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        List<WishlistItemResponseDTO> result = wishlistService.getUserWishlist(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(wishlistRepository, times(1)).findByUserId(userId);
        verify(wishlistMapper, never()).toResponseDTO(any(Wishlist.class));
    }

    @Test
    void getUserWishlist_ShouldReturnMultipleItems() {
        // Arrange
        Wishlist wishlist2 = Wishlist.builder()
                .id(2L)
                .userId(userId)
                .productId(101L)
                .build();

        WishlistItemResponseDTO responseDTO2 = WishlistItemResponseDTO.builder()
                .id(2L)
                .productId(101L)
                .name("Cat Toy")
                .img("toy.png")
                .price(new BigDecimal("9.99"))
                .color("Red")
                .size("S")
                .build();

        List<Wishlist> wishlistItems = Arrays.asList(wishlist, wishlist2);
        when(wishlistRepository.findByUserId(userId)).thenReturn(wishlistItems);
        when(wishlistMapper.toResponseDTO(wishlist)).thenReturn(responseDTO);
        when(wishlistMapper.toResponseDTO(wishlist2)).thenReturn(responseDTO2);

        // Act
        List<WishlistItemResponseDTO> result = wishlistService.getUserWishlist(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(wishlistRepository, times(1)).findByUserId(userId);
        verify(wishlistMapper, times(2)).toResponseDTO(any(Wishlist.class));
    }

    @Test
    void addToWishlist_ShouldAddProductSuccessfully() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);
        when(wishlistMapper.toResponseDTO(any(Wishlist.class), any(Product.class))).thenReturn(responseDTO);

        // Act
        WishlistItemResponseDTO result = wishlistService.addToWishlist(userId, productId);

        // Assert
        assertNotNull(result);
        assertEquals(responseDTO, result);
        assertEquals(productId, result.getProductId());
        assertEquals("Dog Collar", result.getName());
        assertEquals(new BigDecimal("15.99"), result.getPrice());

        verify(productRepository, times(1)).findById(productId);
        verify(wishlistRepository, times(1)).existsByUserIdAndProductId(userId, productId);
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
        verify(wishlistMapper, times(1)).toResponseDTO(any(Wishlist.class), eq(product));
    }

    @Test
    void addToWishlist_ShouldThrowResourceNotFoundException_WhenProductNotFound() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> wishlistService.addToWishlist(userId, productId)
        );

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verify(wishlistRepository, never()).existsByUserIdAndProductId(anyLong(), anyLong());
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void addToWishlist_ShouldThrowResourceAlreadyExistsException_WhenProductAlreadyInWishlist() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

        // Act & Assert
        ResourceAlreadyExistsException exception = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> wishlistService.addToWishlist(userId, productId)
        );

        assertEquals("Product already in wishlist", exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verify(wishlistRepository, times(1)).existsByUserIdAndProductId(userId, productId);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void removeFromWishlist_ShouldRemoveProductSuccessfully() {
        // Arrange
        when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);
        doNothing().when(wishlistRepository).deleteByUserIdAndProductId(userId, productId);

        // Act
        assertDoesNotThrow(() -> wishlistService.removeFromWishlist(userId, productId));

        // Assert
        verify(wishlistRepository, times(1)).existsByUserIdAndProductId(userId, productId);
        verify(wishlistRepository, times(1)).deleteByUserIdAndProductId(userId, productId);
    }

    @Test
    void removeFromWishlist_ShouldThrowResourceNotFoundException_WhenProductNotInWishlist() {
        // Arrange
        when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> wishlistService.removeFromWishlist(userId, productId)
        );

        assertEquals("Product not in wishlist", exception.getMessage());
        verify(wishlistRepository, times(1)).existsByUserIdAndProductId(userId, productId);
        verify(wishlistRepository, never()).deleteByUserIdAndProductId(anyLong(), anyLong());
    }

    @Test
    void isInWishlist_ShouldReturnTrue_WhenProductInWishlist() {
        // Arrange
        when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

        // Act
        boolean result = wishlistService.isInWishlist(userId, productId);

        // Assert
        assertTrue(result);
        verify(wishlistRepository, times(1)).existsByUserIdAndProductId(userId, productId);
    }

    @Test
    void isInWishlist_ShouldReturnFalse_WhenProductNotInWishlist() {
        // Arrange
        when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);

        // Act
        boolean result = wishlistService.isInWishlist(userId, productId);

        // Assert
        assertFalse(result);
        verify(wishlistRepository, times(1)).existsByUserIdAndProductId(userId, productId);
    }

    @Test
    void clearWishlist_ShouldDeleteAllItemsForUser() {
        // Arrange
        doNothing().when(wishlistRepository).deleteByUserId(userId);

        // Act
        assertDoesNotThrow(() -> wishlistService.clearWishlist(userId));

        // Assert
        verify(wishlistRepository, times(1)).deleteByUserId(userId);
    }

    @Test
    void addToWishlist_ShouldHandleProductWithAllFields() {
        // Arrange
        Product fullProduct = Product.builder()
                .id(200L)
                .name("Premium Cat Bed")
                .size(SizeType.L)
                .pet(PetType.CAT)
                .price(new BigDecimal("45.50"))
                .img("cat-bed.png")
                .color("Blue")
                .build();

        WishlistItemResponseDTO fullResponseDTO = WishlistItemResponseDTO.builder()
                .id(5L)
                .productId(200L)
                .name("Premium Cat Bed")
                .img("cat-bed.png")
                .price(new BigDecimal("45.50"))
                .color("Blue")
                .size("L")
                .build();

        when(productRepository.findById(200L)).thenReturn(Optional.of(fullProduct));
        when(wishlistRepository.existsByUserIdAndProductId(userId, 200L)).thenReturn(false);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);
        when(wishlistMapper.toResponseDTO(any(Wishlist.class), any(Product.class))).thenReturn(fullResponseDTO);

        // Act
        WishlistItemResponseDTO result = wishlistService.addToWishlist(userId, 200L);

        // Assert
        assertNotNull(result);
        assertEquals("Premium Cat Bed", result.getName());
        assertEquals(new BigDecimal("45.50"), result.getPrice());
        assertEquals("Blue", result.getColor());
        assertEquals("L", result.getSize());
        assertEquals("cat-bed.png", result.getImg());
    }
}
