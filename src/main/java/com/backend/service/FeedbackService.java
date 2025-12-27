package com.backend.service;

import com.backend.dto.FeedbackDTO;
import com.backend.exception.ResourceNotFoundException;
import com.backend.mapper.FeedbackMapper;
import com.backend.model.Feedback;
import com.backend.model.User;
import com.backend.repository.FeedbackRepository;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final FeedbackMapper feedbackMapper;

    private static final String FEEDBACK_NOT_FOUND = "Feedback not found with id: ";
    private static final String USER_NOT_FOUND = "User not found";

    @Transactional
    public FeedbackDTO createFeedback(FeedbackDTO feedbackDTO, Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Create feedback attempt by user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Create feedback failed - user not found: {}", userEmail);
                    return new ResourceNotFoundException(USER_NOT_FOUND);
                });

        Feedback feedback = Feedback.builder()
                .feedbackText(feedbackDTO.getFeedbackText())
                .createdAt(LocalDateTime.now())
                .userId(user.getId())
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback created successfully: feedbackId={}, user={}", saved.getId(), userEmail);
        return feedbackMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public FeedbackDTO getFeedbackById(Integer id) {
        log.info("Fetching feedback by id: {}", id);
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Feedback not found: feedbackId={}", id);
                    return new ResourceNotFoundException(FEEDBACK_NOT_FOUND + id);
                });
        return feedbackMapper.toDto(feedback);
    }


    @Transactional(readOnly = true)
    public List<FeedbackDTO> getFeedbackByUserId(Long userId) {
        log.info("Fetching feedback for userId: {}", userId);
        List<FeedbackDTO> feedbackList = feedbackRepository.findByUserId(userId).stream()
                .map(feedbackMapper::toDto)
                .toList();
        log.debug("Found {} feedback entries for userId={}", feedbackList.size(), userId);
        return feedbackList;
    }

    @Transactional(readOnly = true)
    public List<FeedbackDTO> getAllFeedback() {
        log.info("Fetching all feedback");
        List<FeedbackDTO> feedbackList = feedbackRepository.findAll().stream()
                .map(feedbackMapper::toDto)
                .toList();
        log.debug("Found {} total feedback entries", feedbackList.size());
        return feedbackList;
    }

    @Transactional
    public FeedbackDTO updateFeedback(Integer id, FeedbackDTO feedbackDTO, Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Update feedback attempt by user: {}, feedbackId: {}", userEmail, id);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Update feedback failed - user not found: {}", userEmail);
                    return new ResourceNotFoundException(USER_NOT_FOUND);
                });

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Update feedback failed - feedback not found: feedbackId={}", id);
                    return new ResourceNotFoundException(FEEDBACK_NOT_FOUND + id);
                });

        if (!feedback.getUserId().equals(user.getId())) {
            log.warn("Update feedback denied - user {} trying to update feedbackId {} of another user", userEmail, id);
            throw new AccessDeniedException("You can only update your own feedback");
        }

        feedback.setFeedbackText(feedbackDTO.getFeedbackText());
        Feedback updated = feedbackRepository.save(feedback);
        log.info("Feedback updated successfully: feedbackId={}, user={}", id, userEmail);
        return feedbackMapper.toDto(updated);
    }

    @Transactional
    public void deleteFeedback(Integer id, Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Delete feedback attempt by user: {}, feedbackId: {}", userEmail, id);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Delete feedback failed - user not found: {}", userEmail);
                    return new ResourceNotFoundException(USER_NOT_FOUND);
                });

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Delete feedback failed - feedback not found: feedbackId={}", id);
                    return new ResourceNotFoundException(FEEDBACK_NOT_FOUND + id);
                });

        if (!feedback.getUserId().equals(user.getId())) {
            log.warn("Delete feedback denied - user {} trying to delete feedbackId {} of another user", userEmail, id);
            throw new AccessDeniedException("You can only delete your own feedback");
        }

        feedbackRepository.delete(feedback);
        log.info("Feedback deleted successfully: feedbackId={}, user={}", id, userEmail);
    }
}
