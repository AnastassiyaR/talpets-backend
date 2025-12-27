package com.backend.controller;

import com.backend.AbstractIntegrationTest;
import com.backend.dto.FeedbackDTO;
import com.backend.model.Feedback;
import com.backend.model.User;
import com.backend.repository.FeedbackRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class FeedbackControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PhotoService photoService;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        photoService.cleanUploadFolder();
        feedbackRepository.deleteAll();
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
    }

    // ==================== CREATE FEEDBACK TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void createFeedback_shouldCreateFeedback_whenDataIsValid() throws Exception {
        // GIVEN
        FeedbackDTO dto = FeedbackDTO.builder()
                .feedbackText("Great service!")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.feedbackText").value("Great service!"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.createdAt").exists());

        List<Feedback> feedbacks = feedbackRepository.findAll();
        assertEquals(1, feedbacks.size());
        assertEquals("Great service!", feedbacks.get(0).getFeedbackText());
        assertEquals(testUser.getId(), feedbacks.get(0).getUserId());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void createFeedback_shouldReturnBadRequest_whenFeedbackTextIsBlank() throws Exception {
        // GIVEN
        FeedbackDTO dto = FeedbackDTO.builder()
                .feedbackText("")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        List<Feedback> feedbacks = feedbackRepository.findAll();
        assertTrue(feedbacks.isEmpty());
    }

    @Test
    void createFeedback_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // GIVEN
        FeedbackDTO dto = FeedbackDTO.builder()
                .feedbackText("Great service!")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET FEEDBACK BY ID TESTS ====================

    @Test
    void getFeedbackById_shouldReturnFeedback_whenFeedbackExists() throws Exception {
        // GIVEN
        Feedback feedback = Feedback.builder()
                .userId(testUser.getId())
                .feedbackText("Test feedback")
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(feedback);

        // WHEN & THEN
        mockMvc.perform(get("/api/feedback/{id}", feedback.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(feedback.getId()))
                .andExpect(jsonPath("$.feedbackText").value("Test feedback"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    void getFeedbackById_shouldReturnNotFound_whenFeedbackDoesNotExist() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/feedback/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    // ==================== GET FEEDBACK BY USER ID TESTS ====================

    @Test
    void getFeedbackByUserId_shouldReturnEmptyList_whenUserHasNoFeedback() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/feedback/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getFeedbackByUserId_shouldReturnUserFeedback_whenUserHasFeedback() throws Exception {
        // GIVEN
        Feedback feedback1 = Feedback.builder()
                .userId(testUser.getId())
                .feedbackText("Feedback 1")
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(feedback1);

        Feedback feedback2 = Feedback.builder()
                .userId(testUser.getId())
                .feedbackText("Feedback 2")
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(feedback2);

        Feedback anotherUserFeedback = Feedback.builder()
                .userId(anotherUser.getId())
                .feedbackText("Another user feedback")
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(anotherUserFeedback);

        // WHEN & THEN
        mockMvc.perform(get("/api/feedback/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()))
                .andExpect(jsonPath("$[1].userId").value(testUser.getId()));
    }

    // ==================== GET ALL FEEDBACK TESTS ====================

    @Test
    void getAllFeedback_shouldReturnEmptyList_whenNoFeedback() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/feedback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllFeedback_shouldReturnAllFeedback_whenFeedbackExists() throws Exception {
        // GIVEN
        Feedback feedback1 = Feedback.builder()
                .userId(testUser.getId())
                .feedbackText("Feedback 1")
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(feedback1);

        Feedback feedback2 = Feedback.builder()
                .userId(anotherUser.getId())
                .feedbackText("Feedback 2")
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(feedback2);

        // WHEN & THEN
        mockMvc.perform(get("/api/feedback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ==================== UPDATE FEEDBACK TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void updateFeedback_shouldUpdateFeedback_whenDataIsValid() throws Exception {
        // GIVEN
        Feedback feedback = Feedback.builder()
                .userId(testUser.getId())
                .feedbackText("Original feedback")
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(feedback);

        FeedbackDTO dto = FeedbackDTO.builder()
                .feedbackText("Updated feedback")
                .build();

        // WHEN & THEN
        mockMvc.perform(put("/api/feedback/{id}", feedback.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(feedback.getId()))
                .andExpect(jsonPath("$.feedbackText").value("Updated feedback"));

        Feedback updatedFeedback = feedbackRepository.findById(feedback.getId()).orElseThrow();
        assertEquals("Updated feedback", updatedFeedback.getFeedbackText());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void updateFeedback_shouldReturnNotFound_whenFeedbackDoesNotExist() throws Exception {
        // GIVEN
        FeedbackDTO dto = FeedbackDTO.builder()
                .feedbackText("Updated feedback")
                .build();

        // WHEN & THEN
        mockMvc.perform(put("/api/feedback/{id}", 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void updateFeedback_shouldReturnForbidden_whenFeedbackBelongsToAnotherUser() throws Exception {
        // GIVEN
        Feedback anotherUserFeedback = Feedback.builder()
                .userId(anotherUser.getId())
                .feedbackText("Another user's feedback")
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(anotherUserFeedback);

        FeedbackDTO dto = FeedbackDTO.builder()
                .feedbackText("Trying to update")
                .build();

        // WHEN & THEN
        mockMvc.perform(put("/api/feedback/{id}", anotherUserFeedback.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        Feedback unchanged = feedbackRepository.findById(anotherUserFeedback.getId()).orElseThrow();
        assertEquals("Another user's feedback", unchanged.getFeedbackText());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void updateFeedback_shouldReturnBadRequest_whenFeedbackTextIsBlank() throws Exception {
        // GIVEN
        Feedback feedback = Feedback.builder()
                .userId(testUser.getId())
                .feedbackText("Original feedback")
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(feedback);

        FeedbackDTO dto = FeedbackDTO.builder()
                .feedbackText("")
                .build();

        // WHEN & THEN
        mockMvc.perform(put("/api/feedback/{id}", feedback.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFeedback_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // GIVEN
        FeedbackDTO dto = FeedbackDTO.builder()
                .feedbackText("Updated feedback")
                .build();

        // WHEN & THEN
        mockMvc.perform(put("/api/feedback/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== DELETE FEEDBACK TESTS ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void deleteFeedback_shouldDeleteFeedback_whenFeedbackExists() throws Exception {
        // GIVEN
        Feedback feedback = Feedback.builder()
                .userId(testUser.getId())
                .feedbackText("To be deleted")
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(feedback);

        // WHEN & THEN
        mockMvc.perform(delete("/api/feedback/{id}", feedback.getId()))
                .andExpect(status().isNoContent());

        assertFalse(feedbackRepository.findById(feedback.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void deleteFeedback_shouldReturnNotFound_whenFeedbackDoesNotExist() throws Exception {
        // WHEN & THEN
        mockMvc.perform(delete("/api/feedback/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void deleteFeedback_shouldReturnForbidden_whenFeedbackBelongsToAnotherUser() throws Exception {
        // GIVEN
        Feedback anotherUserFeedback = Feedback.builder()
                .userId(anotherUser.getId())
                .feedbackText("Another user's feedback")
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(anotherUserFeedback);

        // WHEN & THEN
        mockMvc.perform(delete("/api/feedback/{id}", anotherUserFeedback.getId()))
                .andExpect(status().isForbidden());

        assertTrue(feedbackRepository.findById(anotherUserFeedback.getId()).isPresent());
    }

    @Test
    void deleteFeedback_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // WHEN & THEN
        mockMvc.perform(delete("/api/feedback/{id}", 1))
                .andExpect(status().isUnauthorized());
    }

    // ==================== EDGE CASES ====================

    @Test
    @WithMockUser(username = "test@mail.com")
    void createFeedback_shouldAllowMultipleFeedbacksFromSameUser() throws Exception {
        // GIVEN
        FeedbackDTO dto1 = FeedbackDTO.builder()
                .feedbackText("First feedback")
                .build();

        FeedbackDTO dto2 = FeedbackDTO.builder()
                .feedbackText("Second feedback")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        List<Feedback> feedbacks = feedbackRepository.findByUserId(testUser.getId());
        assertEquals(2, feedbacks.size());
    }

    @Test
    @WithMockUser(username = "test@mail.com")
    void updateFeedback_shouldPreserveCreatedAt_whenUpdatingFeedback() throws Exception {
        // GIVEN
        LocalDateTime originalDate = LocalDateTime.now().minusDays(1);
        Feedback feedback = Feedback.builder()
                .userId(testUser.getId())
                .feedbackText("Original feedback")
                .createdAt(originalDate)
                .build();
        feedbackRepository.save(feedback);

        FeedbackDTO dto = FeedbackDTO.builder()
                .feedbackText("Updated feedback")
                .build();

        // WHEN
        mockMvc.perform(put("/api/feedback/{id}", feedback.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // THEN
        Feedback updatedFeedback = feedbackRepository.findById(feedback.getId()).orElseThrow();
        assertEquals(originalDate, updatedFeedback.getCreatedAt());
        assertEquals("Updated feedback", updatedFeedback.getFeedbackText());
    }
}
