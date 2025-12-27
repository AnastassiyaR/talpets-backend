package com.backend.controller;

import com.backend.AbstractIntegrationTest;
import com.backend.dto.CommentDTO;
import com.backend.model.Comment;
import com.backend.model.PetType;
import com.backend.model.Product;
import com.backend.model.SizeType;
import com.backend.model.User;
import com.backend.repository.CommentRepository;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class CommentControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PhotoService photoService;

    private User testUser;
    private User anotherUser;
    private Product testProduct;
    private Product anotherProduct;

    @BeforeEach
    void setUp() {
        photoService.cleanUploadFolder();
        commentRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("test@mail.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("John")
                .lastName("Doe")
                .build();
        userRepository.save(testUser);

        anotherUser = User.builder()
                .email("another@mail.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Jane")
                .lastName("Smith")
                .build();
        userRepository.save(anotherUser);

        testProduct = Product.builder()
                .name("Cat Collar")
                .size(SizeType.M)
                .pet(PetType.CAT)
                .price(BigDecimal.valueOf(15.99))
                .color("Yellow")
                .img("collar.png")
                .build();
        productRepository.save(testProduct);

        anotherProduct = Product.builder()
                .name("Dog Bow")
                .size(SizeType.S)
                .pet(PetType.DOG)
                .price(BigDecimal.valueOf(8.99))
                .color("Pink")
                .img("bow.png")
                .build();
        productRepository.save(anotherProduct);
    }

    // ==================== GET COMMENTS BY PRODUCT TESTS ====================

    @Test
    void getCommentsByProduct_shouldReturnEmptyList_whenNoComments() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/comments/product/{productId}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCommentsByProduct_shouldReturnComments_whenCommentsExist() throws Exception {
        // GIVEN
        Comment comment1 = Comment.builder()
                .productId(testProduct.getId())
                .userId(testUser.getId())
                .commentText("Great product!")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .productId(testProduct.getId())
                .userId(anotherUser.getId())
                .commentText("Love it!")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(comment2);

        // WHEN & THEN
        mockMvc.perform(get("/api/comments/product/{productId}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").value(testProduct.getId()))
                .andExpect(jsonPath("$[0].commentText").value("Great product!"))
                .andExpect(jsonPath("$[1].commentText").value("Love it!"));
    }

    @Test
    void getCommentsByProduct_shouldReturnOnlyProductComments_whenMultipleProductsHaveComments() throws Exception {
        // GIVEN
        Comment productComment = Comment.builder()
                .productId(testProduct.getId())
                .userId(testUser.getId())
                .commentText("Comment for test product")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(productComment);

        Comment anotherProductComment = Comment.builder()
                .productId(anotherProduct.getId())
                .userId(testUser.getId())
                .commentText("Comment for another product")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(anotherProductComment);

        // WHEN & THEN
        mockMvc.perform(get("/api/comments/product/{productId}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].commentText").value("Comment for test product"));
    }

    // ==================== CREATE COMMENT TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void createComment_shouldCreateComment_whenDataIsValid() throws Exception {
        // GIVEN
        CommentDTO dto = CommentDTO.builder()
                .productId(testProduct.getId())
                .commentText("Amazing quality!")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.commentText").value("Amazing quality!"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.createdDate").exists());

        List<Comment> comments = commentRepository.findByProductId(testProduct.getId());
        assertEquals(1, comments.size());
        assertEquals("Amazing quality!", comments.get(0).getCommentText());
        assertEquals(testUser.getId(), comments.get(0).getUserId());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void createComment_shouldReturnBadRequest_whenCommentTextIsBlank() throws Exception {
        // GIVEN
        CommentDTO dto = CommentDTO.builder()
                .productId(testProduct.getId())
                .commentText("")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        List<Comment> comments = commentRepository.findAll();
        assertTrue(comments.isEmpty());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void createComment_shouldReturnBadRequest_whenCommentTextIsTooLong() throws Exception {
        // GIVEN
        String longText = "a".repeat(1001);
        CommentDTO dto = CommentDTO.builder()
                .productId(testProduct.getId())
                .commentText(longText)
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void createComment_shouldReturnBadRequest_whenProductIdIsNull() throws Exception {
        // GIVEN
        CommentDTO dto = CommentDTO.builder()
                .productId(null)
                .commentText("Great product!")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // GIVEN
        CommentDTO dto = CommentDTO.builder()
                .productId(testProduct.getId())
                .commentText("Great product!")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== UPDATE COMMENT TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void updateComment_shouldReturnBadRequest_whenCommentTextIsBlank() throws Exception {
        // GIVEN
        Comment comment = Comment.builder()
                .productId(testProduct.getId())
                .userId(testUser.getId())
                .commentText("Original text")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(comment);

        CommentDTO dto = CommentDTO.builder()
                .commentText("")
                .build();

        // WHEN & THEN
        mockMvc.perform(put("/api/comments/{id}", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ==================== DELETE COMMENT TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void deleteComment_shouldDeleteComment_whenCommentExists() throws Exception {
        // GIVEN
        Comment comment = Comment.builder()
                .productId(testProduct.getId())
                .userId(testUser.getId())
                .commentText("To be deleted")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(comment);

        // WHEN & THEN
        mockMvc.perform(delete("/api/comments/{id}", comment.getId()))
                .andExpect(status().isNoContent());

        assertFalse(commentRepository.findById(comment.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void deleteComment_shouldReturnNotFound_whenCommentDoesNotExist() throws Exception {
        // WHEN & THEN
        mockMvc.perform(delete("/api/comments/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void deleteComment_shouldReturnForbidden_whenCommentBelongsToAnotherUser() throws Exception {
        // GIVEN
        Comment anotherUserComment = Comment.builder()
                .productId(testProduct.getId())
                .userId(anotherUser.getId())
                .commentText("Another user's comment")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(anotherUserComment);

        // WHEN & THEN
        mockMvc.perform(delete("/api/comments/{id}", anotherUserComment.getId()))
                .andExpect(status().isForbidden());

        assertTrue(commentRepository.findById(anotherUserComment.getId()).isPresent());
    }

    @Test
    void deleteComment_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // WHEN & THEN
        mockMvc.perform(delete("/api/comments/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET ALL COMMENTS TESTS ====================

    @Test
    void getAllComments_shouldReturnEmptyList_whenNoComments() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllComments_shouldReturnAllComments_whenCommentsExist() throws Exception {
        // GIVEN
        Comment comment1 = Comment.builder()
                .productId(testProduct.getId())
                .userId(testUser.getId())
                .commentText("Comment 1")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .productId(anotherProduct.getId())
                .userId(anotherUser.getId())
                .commentText("Comment 2")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(comment2);

        // WHEN & THEN
        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ==================== GET COMMENT BY ID TESTS ====================

    @Test
    void getCommentById_shouldReturnComment_whenCommentExists() throws Exception {
        // GIVEN
        Comment comment = Comment.builder()
                .productId(testProduct.getId())
                .userId(testUser.getId())
                .commentText("Test comment")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(comment);

        // WHEN & THEN
        mockMvc.perform(get("/api/comments/{id}", comment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.commentText").value("Test comment"))
                .andExpect(jsonPath("$.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    void getCommentById_shouldReturnNotFound_whenCommentDoesNotExist() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/comments/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ==================== GET COMMENTS BY USER TESTS ====================

    @Test
    void getCommentsByUser_shouldReturnEmptyList_whenUserHasNoComments() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/comments/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCommentsByUser_shouldReturnUserComments_whenUserHasComments() throws Exception {
        // GIVEN
        Comment comment1 = Comment.builder()
                .productId(testProduct.getId())
                .userId(testUser.getId())
                .commentText("User comment 1")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .productId(anotherProduct.getId())
                .userId(testUser.getId())
                .commentText("User comment 2")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(comment2);

        Comment anotherUserComment = Comment.builder()
                .productId(testProduct.getId())
                .userId(anotherUser.getId())
                .commentText("Another user comment")
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(anotherUserComment);

        // WHEN & THEN
        mockMvc.perform(get("/api/comments/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()))
                .andExpect(jsonPath("$[1].userId").value(testUser.getId()));
    }

    // ==================== EDGE CASES ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void createComment_shouldAllowMultipleCommentsFromSameUser_onSameProduct() throws Exception {
        // GIVEN
        CommentDTO dto1 = CommentDTO.builder()
                .productId(testProduct.getId())
                .commentText("First comment")
                .build();

        CommentDTO dto2 = CommentDTO.builder()
                .productId(testProduct.getId())
                .commentText("Second comment")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        List<Comment> comments = commentRepository.findByProductId(testProduct.getId());
        assertEquals(2, comments.size());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void createComment_shouldWorkWith1000Characters_whenAtMaxLength() throws Exception {
        String maxLengthText = "a".repeat(1000);
        CommentDTO dto = CommentDTO.builder()
                .productId(testProduct.getId())
                .commentText(maxLengthText)
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentText").value(maxLengthText));

        List<Comment> comments = commentRepository.findAll();
        assertEquals(1, comments.size());
        assertEquals(1000, comments.get(0).getCommentText().length());
    }
}
