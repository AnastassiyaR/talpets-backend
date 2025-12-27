package com.backend.service;

import com.backend.dto.ProductResponseDTO;
import com.backend.exception.ResourceNotFoundException;
import com.backend.mapper.ProductMapper;
import com.backend.model.PetType;
import com.backend.model.Product;
import com.backend.model.SizeType;
import com.backend.repository.ProductRepository;
import com.backend.specification.ProductSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductResponseDTO productResponseDTO;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Dog Collar")
                .size(SizeType.M)
                .pet(PetType.DOG)
                .price(new BigDecimal("15.99"))
                .img("collar.png")
                .color("Yellow")
                .build();

        productResponseDTO = new ProductResponseDTO(
                1L,
                "Dog Collar",
                SizeType.M,
                PetType.DOG,
                "Yellow",
                new BigDecimal("15.99"),
                "collar.png"
        );
    }

    // FIND PRODUCTS TESTS

    @Test
    void findProducts_shouldReturnAllProducts_whenNoFiltersApplied() {
        // GIVEN
        List<Product> products = Arrays.asList(product);
        List<ProductResponseDTO> expectedDtos = Arrays.asList(productResponseDTO);

        given(productRepository.findAll(any(Specification.class)))
                .willReturn(products);
        given(productMapper.toFilterDtoList(products))
                .willReturn(expectedDtos);

        // WHEN
        List<ProductResponseDTO> result = productService.findProducts(null, null, null, null);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productResponseDTO, result.get(0));

        then(productRepository).should().findAll(any(Specification.class));
        then(productMapper).should().toFilterDtoList(products);
    }

    @Test
    void findProducts_shouldReturnFilteredProducts_whenSizeFilterApplied() {
        // GIVEN
        List<SizeType> sizes = Arrays.asList(SizeType.M, SizeType.L);
        List<Product> products = Arrays.asList(product);
        List<ProductResponseDTO> expectedDtos = Arrays.asList(productResponseDTO);

        given(productRepository.findAll(any(Specification.class)))
                .willReturn(products);
        given(productMapper.toFilterDtoList(products))
                .willReturn(expectedDtos);

        // WHEN
        List<ProductResponseDTO> result = productService.findProducts(sizes, null, null, null);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());

        then(productRepository).should().findAll(any(Specification.class));
        then(productMapper).should().toFilterDtoList(products);
    }

    @Test
    void findProducts_shouldReturnFilteredProducts_whenPetFilterApplied() {
        // GIVEN
        List<PetType> pets = Arrays.asList(PetType.DOG);
        List<Product> products = Arrays.asList(product);
        List<ProductResponseDTO> expectedDtos = Arrays.asList(productResponseDTO);

        given(productRepository.findAll(any(Specification.class)))
                .willReturn(products);
        given(productMapper.toFilterDtoList(products))
                .willReturn(expectedDtos);

        // WHEN
        List<ProductResponseDTO> result = productService.findProducts(null, pets, null, null);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());

        then(productRepository).should().findAll(any(Specification.class));
        then(productMapper).should().toFilterDtoList(products);
    }

    @Test
    void findProducts_shouldReturnFilteredProducts_whenColorFilterApplied() {
        // GIVEN
        List<String> colors = Arrays.asList("Yellow", "Red");
        List<Product> products = Arrays.asList(product);
        List<ProductResponseDTO> expectedDtos = Arrays.asList(productResponseDTO);

        given(productRepository.findAll(any(Specification.class)))
                .willReturn(products);
        given(productMapper.toFilterDtoList(products))
                .willReturn(expectedDtos);

        // WHEN
        List<ProductResponseDTO> result = productService.findProducts(null, null, colors, null);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());

        then(productRepository).should().findAll(any(Specification.class));
        then(productMapper).should().toFilterDtoList(products);
    }

    @Test
    void findProducts_shouldReturnFilteredProducts_whenSearchApplied() {
        // GIVEN
        String search = "Collar";
        List<Product> products = Arrays.asList(product);
        List<ProductResponseDTO> expectedDtos = Arrays.asList(productResponseDTO);

        given(productRepository.findAll(any(Specification.class)))
                .willReturn(products);
        given(productMapper.toFilterDtoList(products))
                .willReturn(expectedDtos);

        // WHEN
        List<ProductResponseDTO> result = productService.findProducts(null, null, null, search);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());

        then(productRepository).should().findAll(any(Specification.class));
        then(productMapper).should().toFilterDtoList(products);
    }

    @Test
    void findProducts_shouldReturnFilteredProducts_whenAllFiltersApplied() {
        // GIVEN
        List<SizeType> sizes = Arrays.asList(SizeType.M);
        List<PetType> pets = Arrays.asList(PetType.DOG);
        List<String> colors = Arrays.asList("Yellow");
        String search = "Collar";
        List<Product> products = Arrays.asList(product);
        List<ProductResponseDTO> expectedDtos = Arrays.asList(productResponseDTO);

        given(productRepository.findAll(any(Specification.class)))
                .willReturn(products);
        given(productMapper.toFilterDtoList(products))
                .willReturn(expectedDtos);

        // WHEN
        List<ProductResponseDTO> result = productService.findProducts(sizes, pets, colors, search);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());

        then(productRepository).should().findAll(any(Specification.class));
        then(productMapper).should().toFilterDtoList(products);
    }

    @Test
    void findProducts_shouldReturnEmptyList_whenNoProductsMatch() {
        // GIVEN
        given(productRepository.findAll(any(Specification.class)))
                .willReturn(Collections.emptyList());
        given(productMapper.toFilterDtoList(Collections.emptyList()))
                .willReturn(Collections.emptyList());

        // WHEN
        List<ProductResponseDTO> result = productService.findProducts(null, null, null, null);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());

        then(productRepository).should().findAll(any(Specification.class));
        then(productMapper).should().toFilterDtoList(Collections.emptyList());
    }

    @Test
    void findProducts_shouldIgnoreEmptyFilters() {
        // GIVEN
        List<Product> products = Arrays.asList(product);
        List<ProductResponseDTO> expectedDtos = Arrays.asList(productResponseDTO);

        given(productRepository.findAll(any(Specification.class)))
                .willReturn(products);
        given(productMapper.toFilterDtoList(products))
                .willReturn(expectedDtos);

        // WHEN
        List<ProductResponseDTO> result = productService.findProducts(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                "  "
        );

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());

        then(productRepository).should().findAll(any(Specification.class));
    }

    // GET PRODUCT BY ID TESTS

    @Test
    void getProductById_shouldReturnProduct_whenProductExists() {
        // GIVEN
        Long productId = 1L;
        given(productRepository.findById(productId))
                .willReturn(Optional.of(product));
        given(productMapper.toFilterDto(product))
                .willReturn(productResponseDTO);

        // WHEN
        ProductResponseDTO result = productService.getProductById(productId);

        // THEN
        assertNotNull(result);
        assertEquals(productResponseDTO, result);
        assertEquals(productId, result.getId());
        assertEquals("Dog Collar", result.getName());

        then(productRepository).should().findById(productId);
        then(productMapper).should().toFilterDto(product);
    }

    @Test
    void getProductById_shouldThrowResourceNotFoundException_whenProductNotFound() {
        // GIVEN
        Long productId = 999L;
        given(productRepository.findById(productId))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(productId)
        );

        assertEquals("Product with id 999 not found", exception.getMessage());

        then(productRepository).should().findById(productId);
        then(productMapper).should(never()).toFilterDto(any());
    }

    // CREATE PRODUCT TESTS

    @Test
    void createProduct_shouldCreateProductSuccessfully() {
        // GIVEN
        ProductResponseDTO newProductDto = new ProductResponseDTO(
                null,
                "Cat Toy",
                SizeType.S,
                PetType.CAT,
                "Red",
                new BigDecimal("9.99"),
                "toy.png"
        );

        Product newProduct = Product.builder()
                .name("Cat Toy")
                .size(SizeType.S)
                .pet(PetType.CAT)
                .color("Red")
                .price(new BigDecimal("9.99"))
                .img("toy.png")
                .build();

        Product savedProduct = Product.builder()
                .id(2L)
                .name("Cat Toy")
                .size(SizeType.S)
                .pet(PetType.CAT)
                .color("Red")
                .price(new BigDecimal("9.99"))
                .img("toy.png")
                .build();

        ProductResponseDTO savedProductDto = new ProductResponseDTO(
                2L,
                "Cat Toy",
                SizeType.S,
                PetType.CAT,
                "Red",
                new BigDecimal("9.99"),
                "toy.png"
        );

        given(productMapper.toEntity(newProductDto))
                .willReturn(newProduct);
        given(productRepository.save(newProduct))
                .willReturn(savedProduct);
        given(productMapper.toFilterDto(savedProduct))
                .willReturn(savedProductDto);

        // WHEN
        ProductResponseDTO result = productService.createProduct(newProductDto);

        // THEN
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Cat Toy", result.getName());
        assertEquals(SizeType.S, result.getSize());
        assertEquals(PetType.CAT, result.getPet());

        then(productMapper).should().toEntity(newProductDto);
        then(productRepository).should().save(newProduct);
        then(productMapper).should().toFilterDto(savedProduct);
    }

    // UPDATE PRODUCT TESTS

    @Test
    void updateProduct_shouldUpdateProductSuccessfully() {
        // GIVEN
        Long productId = 1L;
        ProductResponseDTO updateDto = new ProductResponseDTO(
                1L,
                "Updated Collar",
                SizeType.L,
                PetType.DOG,
                "Blue",
                new BigDecimal("19.99"),
                "new-collar.png"
        );

        given(productRepository.findById(productId))
                .willReturn(Optional.of(product));
        doNothing().when(productMapper).updateProductFromDto(updateDto, product);
        given(productMapper.toFilterDto(product))
                .willReturn(updateDto);

        // WHEN
        ProductResponseDTO result = productService.updateProduct(productId, updateDto);

        // THEN
        assertNotNull(result);
        assertEquals(updateDto, result);

        then(productRepository).should().findById(productId);
        then(productMapper).should().updateProductFromDto(updateDto, product);
        then(productMapper).should().toFilterDto(product);
    }

    @Test
    void updateProduct_shouldThrowResourceNotFoundException_whenProductNotFound() {
        // GIVEN
        Long productId = 999L;
        ProductResponseDTO updateDto = new ProductResponseDTO(
                999L,
                "Updated Product",
                SizeType.M,
                PetType.DOG,
                "Red",
                new BigDecimal("25.99"),
                "image.png"
        );

        given(productRepository.findById(productId))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(productId, updateDto)
        );

        assertEquals("Product with id 999 not found", exception.getMessage());

        then(productRepository).should().findById(productId);
        then(productMapper).should(never()).updateProductFromDto(any(), any());
        then(productMapper).should(never()).toFilterDto(any());
    }

    // DELETE PRODUCT TESTS

    @Test
    void deleteProduct_shouldDeleteProductSuccessfully() {
        // GIVEN
        Long productId = 1L;
        given(productRepository.existsById(productId))
                .willReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        // WHEN
        assertDoesNotThrow(() -> productService.deleteProduct(productId));

        // THEN
        then(productRepository).should().existsById(productId);
        then(productRepository).should().deleteById(productId);
    }

    @Test
    void deleteProduct_shouldThrowResourceNotFoundException_whenProductNotFound() {
        // GIVEN
        Long productId = 999L;
        given(productRepository.existsById(productId))
                .willReturn(false);

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(productId)
        );

        assertEquals("Product with id 999 not found", exception.getMessage());

        then(productRepository).should().existsById(productId);
        then(productRepository).should(never()).deleteById(anyLong());
    }
}
