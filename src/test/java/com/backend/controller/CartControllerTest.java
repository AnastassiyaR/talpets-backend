package com.backend.controller;

import com.backend.AbstractIntegrationTest;
import com.backend.dto.CartDTO;
import com.backend.model.Cart;
import com.backend.model.PetType;
import com.backend.model.Product;
import com.backend.model.SizeType;
import com.backend.model.User;
import com.backend.repository.CartRepository;
import com.backend.repository.ProductRepository;
import com.backend.repository.UserRepository;
import com.backend.service.PhotoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class CartControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PhotoService photoService;

    private User testUser;
    private Product catCollar;
    private Product dogBow;
    private Product catSunglasses;

    @BeforeEach
    void setUp() {
        photoService.cleanUploadFolder();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("test@mail.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("John")
                .lastName("Doe")
                .build();
        userRepository.save(testUser);

        catCollar = Product.builder()
                .name("Collar")
                .size(SizeType.M)
                .pet(PetType.CAT)
                .price(BigDecimal.valueOf(15.99))
                .color("Yellow")
                .img("collar.png")
                .build();
        productRepository.save(catCollar);

        dogBow = Product.builder()
                .name("Bow")
                .size(SizeType.S)
                .pet(PetType.DOG)
                .price(BigDecimal.valueOf(8.99))
                .color("Pink")
                .img("bow.png")
                .build();
        productRepository.save(dogBow);

        catSunglasses = Product.builder()
                .name("Sunglasses")
                .size(SizeType.XS)
                .pet(PetType.CAT)
                .price(BigDecimal.valueOf(12.99))
                .color("Pink")
                .img("sunglasses.png")
                .build();
        productRepository.save(catSunglasses);
    }

    // ==================== GET CART TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void getCart_shouldReturnEmptyCart_whenCartIsEmpty() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void getCart_shouldReturnCartItems_whenCartHasItems() throws Exception {
        // GIVEN - добавляем товары в корзину
        Cart cartItem1 = Cart.builder()
                .userId(testUser.getId())
                .productId(catCollar.getId())
                .quantity(2)
                .selectedSize("M")
                .build();
        cartRepository.save(cartItem1);

        Cart cartItem2 = Cart.builder()
                .userId(testUser.getId())
                .productId(dogBow.getId())
                .quantity(1)
                .selectedSize("S")
                .build();
        cartRepository.save(cartItem2);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].selectedSize").value("M"))
                .andExpect(jsonPath("$[1].userId").value(testUser.getId()))
                .andExpect(jsonPath("$[1].quantity").value(1))
                .andExpect(jsonPath("$[1].selectedSize").value("S"));
    }

    @Test
    void getCart_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== ADD TO CART TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void addToCart_shouldAddNewItem_whenDataIsValid() throws Exception {
        // GIVEN
        CartDTO dto = CartDTO.builder()
                .productId(catCollar.getId())
                .quantity(2)
                .selectedSize("M")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(catCollar.getId()))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.selectedSize").value("M"));

        List<Cart> cartItems = cartRepository.findByUserId(testUser.getId());
        assertEquals(1, cartItems.size());
        assertEquals(2, cartItems.get(0).getQuantity());
        assertEquals("M", cartItems.get(0).getSelectedSize());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void addToCart_shouldIncreaseQuantity_whenProductAlreadyInCart() throws Exception {
        // GIVEN - товар уже в корзине
        Cart existingItem = Cart.builder()
                .userId(testUser.getId())
                .productId(catCollar.getId())
                .quantity(1)
                .selectedSize("M")
                .build();
        cartRepository.save(existingItem);

        CartDTO dto = CartDTO.builder()
                .productId(catCollar.getId())
                .quantity(2)
                .selectedSize("M")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3)); // 1 + 2 = 3

        List<Cart> cartItems = cartRepository.findByUserId(testUser.getId());
        assertEquals(1, cartItems.size()); // Всё еще один item
        assertEquals(3, cartItems.get(0).getQuantity()); // Но количество увеличилось
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void addToCart_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        // GIVEN
        CartDTO dto = CartDTO.builder()
                .productId(99999L) // Несуществующий товар
                .quantity(1)
                .selectedSize("M")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToCart_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // GIVEN
        CartDTO dto = CartDTO.builder()
                .productId(catCollar.getId())
                .quantity(1)
                .selectedSize("M")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== UPDATE QUANTITY TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void updateQuantity_shouldUpdateQuantity_whenDataIsValid() throws Exception {
        // GIVEN
        Cart cartItem = Cart.builder()
                .userId(testUser.getId())
                .productId(catCollar.getId())
                .quantity(2)
                .selectedSize("M")
                .build();
        cartRepository.save(cartItem);

        // WHEN & THEN
        mockMvc.perform(put("/api/cart/items/{cartId}", cartItem.getId())
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));

        Cart updatedItem = cartRepository.findById(cartItem.getId()).orElseThrow();
        assertEquals(5, updatedItem.getQuantity());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void updateQuantity_shouldReturnNotFound_whenCartItemDoesNotExist() throws Exception {
        // WHEN & THEN
        mockMvc.perform(put("/api/cart/items/{cartId}", 99999L)
                        .param("quantity", "5"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void updateQuantity_shouldReturnForbidden_whenCartItemBelongsToAnotherUser() throws Exception {
        User anotherUser = User.builder()
                .email("another@mail.com")
                .password(passwordEncoder.encode("password"))
                .firstName("Jane")
                .lastName("Smith")
                .build();
        userRepository.save(anotherUser);

        Cart anotherUserCart = Cart.builder()
                .userId(anotherUser.getId())
                .productId(catCollar.getId())
                .quantity(1)
                .selectedSize("M")
                .build();
        cartRepository.save(anotherUserCart);

        mockMvc.perform(put("/api/cart/items/{cartId}", anotherUserCart.getId())
                        .param("quantity", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateQuantity_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // WHEN & THEN
        mockMvc.perform(put("/api/cart/items/{cartId}", 1L)
                        .param("quantity", "5"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== REMOVE ITEM TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void removeItem_shouldRemoveItem_whenItemExists() throws Exception {
        // GIVEN
        Cart cartItem = Cart.builder()
                .userId(testUser.getId())
                .productId(catCollar.getId())
                .quantity(2)
                .selectedSize("M")
                .build();
        cartRepository.save(cartItem);

        // WHEN & THEN
        mockMvc.perform(delete("/api/cart/items/{cartId}", cartItem.getId()))
                .andExpect(status().isNoContent());

        assertFalse(cartRepository.findById(cartItem.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void removeItem_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        // WHEN & THEN
        mockMvc.perform(delete("/api/cart/items/{cartId}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void removeItem_shouldReturnForbidden_whenItemBelongsToAnotherUser() throws Exception {
        User anotherUser = User.builder()
                .email("another@mail.com")
                .password(passwordEncoder.encode("password"))
                .firstName("Jane")
                .lastName("Smith")
                .build();
        userRepository.save(anotherUser);

        Cart anotherUserCart = Cart.builder()
                .userId(anotherUser.getId())
                .productId(catCollar.getId())
                .quantity(1)
                .selectedSize("M")
                .build();
        cartRepository.save(anotherUserCart);

        // WHEN & THEN
        mockMvc.perform(delete("/api/cart/items/{cartId}", anotherUserCart.getId()))
                .andExpect(status().isForbidden());

        assertTrue(cartRepository.findById(anotherUserCart.getId()).isPresent());
    }

    @Test
    void removeItem_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // WHEN & THEN
        mockMvc.perform(delete("/api/cart/items/{cartId}", 1L))
                .andExpect(status().isUnauthorized());
    }

    // ==================== CLEAR CART TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void clearCart_shouldRemoveAllItems_whenCartHasItems() throws Exception {
        Cart cartItem1 = Cart.builder()
                .userId(testUser.getId())
                .productId(catCollar.getId())
                .quantity(2)
                .selectedSize("M")
                .build();
        cartRepository.save(cartItem1);

        Cart cartItem2 = Cart.builder()
                .userId(testUser.getId())
                .productId(dogBow.getId())
                .quantity(1)
                .selectedSize("S")
                .build();
        cartRepository.save(cartItem2);

        // WHEN & THEN
        mockMvc.perform(delete("/api/cart/clear"))
                .andExpect(status().isNoContent());

        List<Cart> cartItems = cartRepository.findByUserId(testUser.getId());
        assertTrue(cartItems.isEmpty());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void clearCart_shouldSucceed_whenCartIsAlreadyEmpty() throws Exception {
        // WHEN & THEN
        mockMvc.perform(delete("/api/cart/clear"))
                .andExpect(status().isNoContent());

        List<Cart> cartItems = cartRepository.findByUserId(testUser.getId());
        assertTrue(cartItems.isEmpty());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void clearCart_shouldOnlyClearCurrentUserCart_whenMultipleUsersHaveCarts() throws Exception {
        User anotherUser = User.builder()
                .email("another@mail.com")
                .password(passwordEncoder.encode("password"))
                .firstName("Jane")
                .lastName("Smith")
                .build();
        userRepository.save(anotherUser);

        Cart testUserCart = Cart.builder()
                .userId(testUser.getId())
                .productId(catCollar.getId())
                .quantity(1)
                .selectedSize("M")
                .build();
        cartRepository.save(testUserCart);

        Cart anotherUserCart = Cart.builder()
                .userId(anotherUser.getId())
                .productId(dogBow.getId())
                .quantity(1)
                .selectedSize("S")
                .build();
        cartRepository.save(anotherUserCart);

        // WHEN & THEN
        mockMvc.perform(delete("/api/cart/clear"))
                .andExpect(status().isNoContent());

        List<Cart> testUserCartItems = cartRepository.findByUserId(testUser.getId());
        assertTrue(testUserCartItems.isEmpty());

        List<Cart> anotherUserCartItems = cartRepository.findByUserId(anotherUser.getId());
        assertEquals(1, anotherUserCartItems.size());
    }

    @Test
    void clearCart_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // WHEN & THEN
        mockMvc.perform(delete("/api/cart/clear"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET CART TOTAL TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void getCartTotal_shouldReturnZero_whenCartIsEmpty() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/cart/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));
    }

    @Test
    void getCartTotal_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/cart/total"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== EDGE CASES ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void addToCart_shouldAddSeparateItems_whenSameProductWithDifferentSizes() throws Exception {
        Cart existingItem = Cart.builder()
                .userId(testUser.getId())
                .productId(catCollar.getId())
                .quantity(1)
                .selectedSize("M")
                .build();
        cartRepository.save(existingItem);

        CartDTO dto = CartDTO.builder()
                .productId(catCollar.getId())
                .quantity(2)
                .selectedSize("L") // Другой размер!
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        List<Cart> cartItems = cartRepository.findByUserId(testUser.getId());
        assertEquals(2, cartItems.size());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void addToCart_shouldWorkWithPetProducts_whenAddingMultipleProductsForDifferentPets() throws Exception {
        CartDTO catProduct = CartDTO.builder()
                .productId(catCollar.getId()) // для кошки
                .quantity(1)
                .selectedSize("M")
                .build();

        CartDTO dogProduct = CartDTO.builder()
                .productId(dogBow.getId())
                .quantity(2)
                .selectedSize("S")
                .build();

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catProduct)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dogProduct)))
                .andExpect(status().isOk());

        List<Cart> cartItems = cartRepository.findByUserId(testUser.getId());
        assertEquals(2, cartItems.size());
    }
}
