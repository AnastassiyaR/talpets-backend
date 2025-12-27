package com.backend.controller;

import com.backend.AbstractIntegrationTest;
import com.backend.model.Product;
import com.backend.model.SizeType;
import com.backend.model.PetType;
import com.backend.model.User;
import com.backend.repository.ProductRepository;
import com.backend.repository.UserRepository;
import com.backend.repository.WishlistRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for WishlistController
 */
@Transactional
class WishlistControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private User user;
    private Product product;

    @BeforeEach
    void setUp() throws Exception {
        wishlistRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder()
                .email("user@mail.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("John")
                .lastName("Doe")
                .build();
        user = userRepository.save(user);

        product = Product.builder()
                .name("Dog Collar")
                .price(BigDecimal.valueOf(19.99))
                .size(SizeType.M)
                .pet(PetType.DOG)
                .color("Red")
                .img("collar.png")
                .build();
        product = productRepository.save(product);

        token = obtainAccessToken("user@mail.com", "password123");
    }

    // GET WISHLIST
    @Test
    void getWishlist_shouldReturnEmptyList_whenWishlistIsEmpty() throws Exception {
        mockMvc.perform(get("/api/wishlist")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ADD TO WISHLIST

    @Test
    void addToWishlist_shouldAddProductSuccessfully() throws Exception {
        mockMvc.perform(post("/api/wishlist/add/{productId}", product.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(product.getId()))
                .andExpect(jsonPath("$.name").value("Dog Collar"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.color").value("Red"));

        assertEquals(1, wishlistRepository.count());
    }

    @Test
    void addToWishlist_shouldReturnConflict_whenProductAlreadyInWishlist() throws Exception {
        mockMvc.perform(post("/api/wishlist/add/{productId}", product.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/wishlist/add/{productId}", product.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Product already in wishlist"));
    }

    // CHECK IN WISHLIST

    @Test
    void checkInWishlist_shouldReturnTrue_whenProductExists() throws Exception {
        mockMvc.perform(post("/api/wishlist/add/{productId}", product.getId())
                .header("Authorization", "Bearer " + token));

        mockMvc.perform(get("/api/wishlist/check/{productId}", product.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkInWishlist_shouldReturnFalse_whenProductNotExists() throws Exception {
        mockMvc.perform(get("/api/wishlist/check/{productId}", product.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // REMOVE FROM WISHLIST
    @Test
    void removeFromWishlist_shouldRemoveProductSuccessfully() throws Exception {
        mockMvc.perform(post("/api/wishlist/add/{productId}", product.getId())
                .header("Authorization", "Bearer " + token));

        mockMvc.perform(delete("/api/wishlist/remove/{productId}", product.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertEquals(0, wishlistRepository.count());
    }

    // CLEAR WISHLIST
    @Test
    void clearWishlist_shouldRemoveAllProducts() throws Exception {
        mockMvc.perform(post("/api/wishlist/add/{productId}", product.getId())
                .header("Authorization", "Bearer " + token));

        mockMvc.perform(delete("/api/wishlist/clear")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertEquals(0, wishlistRepository.count());
    }

    // HELPER
    private String obtainAccessToken(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }
}
