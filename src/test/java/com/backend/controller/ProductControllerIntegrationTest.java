package com.backend.controller;

import com.backend.AbstractIntegrationTest;
import com.backend.model.PetType;
import com.backend.model.Product;
import com.backend.model.SizeType;
import com.backend.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController
 */
@Transactional
class ProductControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private Product dogProduct;
    private Product catProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        dogProduct = Product.builder()
                .name("Dog Toy")
                .size(SizeType.M)
                .pet(PetType.DOG)
                .color("Red")
                .price(BigDecimal.valueOf(19.99))
                .img("dog-toy.png")
                .build();

        catProduct = Product.builder()
                .name("Cat Bowl")
                .size(SizeType.S)
                .pet(PetType.CAT)
                .color("Blue")
                .price(BigDecimal.valueOf(9.99))
                .img("cat-bowl.png")
                .build();

        productRepository.save(dogProduct);
        productRepository.save(catProduct);
    }

    // FILTER

    @Test
    void filterProducts_shouldReturnAllProducts_whenNoFilters() throws Exception {
        mockMvc.perform(get("/api/products/filter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void filterProducts_shouldFilterByPet() throws Exception {
        mockMvc.perform(get("/api/products/filter")
                        .param("pet", "DOG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Dog Toy"));
    }

    @Test
    void filterProducts_shouldFilterBySizeAndColor() throws Exception {
        mockMvc.perform(get("/api/products/filter")
                        .param("size", "S")
                        .param("color", "Blue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Cat Bowl"));
    }

    @Test
    void filterProducts_shouldFilterBySearch() throws Exception {
        mockMvc.perform(get("/api/products/filter")
                        .param("search", "toy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Dog Toy"));
    }

    // GET BY ID

    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", dogProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dog Toy"))
                .andExpect(jsonPath("$.pet").value("DOG"))
                .andExpect(jsonPath("$.size").value("M"));
    }

    @Test
    void getProductById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product with id 999 not found"));
    }

    // CREATE
    @Test
    void createProduct_shouldCreateProductSuccessfully() throws Exception {
        String requestBody = """
                {
                  "name": "Dog Cage",
                  "size": "L",
                  "pet": "DOG",
                  "color": "White",
                  "price": 49.99,
                  "img": "cage.png"
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dog Cage"))
                .andExpect(jsonPath("$.pet").value("DOG"));

        assertEquals(3, productRepository.count());
    }

    // UPDATE

    @Test
    void updateProduct_shouldUpdateProductSuccessfully() throws Exception {
        String updateBody = """
                {
                  "name": "Updated Dog Toy",
                  "size": "L",
                  "pet": "DOG",
                  "color": "Black",
                  "price": 29.99,
                  "img": "updated.png"
                }
                """;

        mockMvc.perform(put("/api/products/{id}", dogProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Dog Toy"))
                .andExpect(jsonPath("$.color").value("Black"))
                .andExpect(jsonPath("$.price").value(29.99));
    }

    @Test
    void updateProduct_shouldReturn404_whenProductNotFound() throws Exception {
        mockMvc.perform(put("/api/products/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product with id 999 not found"));
    }

    // DELETE

    @Test
    void deleteProduct_shouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", dogProduct.getId()))
                .andExpect(status().isNoContent());

        assertEquals(1, productRepository.count());
    }

    @Test
    void deleteProduct_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product with id 999 not found"));
    }
}
