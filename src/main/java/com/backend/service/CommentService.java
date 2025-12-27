package com.backend.service;


import com.backend.dto.CommentDTO;
import com.backend.exception.ResourceNotFoundException;
import com.backend.mapper.CommentMapper;
import com.backend.model.Comment;
import com.backend.model.User;
import com.backend.repository.CommentRepository;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    private static final String COMMENT_NOT_FOUND = "Comment not found with id: ";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String UNAUTHORIZED_MODIFY = "You can only modify your own comments";


    @Transactional
    public CommentDTO createComment(CommentDTO commentDTO, Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Create comment attempt by user: {}, productId: {}", userEmail, commentDTO.getProductId());

        User user = findUserByEmail(userEmail);

        Comment comment = Comment.builder()
                .productId(commentDTO.getProductId())
                .commentText(commentDTO.getCommentText())
                .createdDate(LocalDateTime.now())
                .userId(user.getId())
                .build();

        Comment saved = commentRepository.save(comment);
        log.debug("Comment created successfully: commentId={}, user={}", saved.getId(), userEmail);
        return commentMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public CommentDTO getCommentById(Long id) {
        log.debug("Fetching comment by id: {}", id);
        Comment comment = findCommentById(id);
        return commentMapper.toDto(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByProductId(Long productId) {
        log.debug("Fetching comments for productId: {}", productId);
        List<CommentDTO> comments = commentRepository.findByProductId(productId).stream()
                .map(commentMapper::toDto)
                .toList();
        log.debug("Found {} comments for productId: {}", comments.size(), productId);
        return comments;
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByUserId(Long userId) {
        log.debug("Fetching comments for userId: {}", userId);
        List<CommentDTO> comments = commentRepository.findByUserId(userId).stream()
                .map(commentMapper::toDto)
                .toList();
        log.debug("Found {} comments for userId: {}", comments.size(), userId);
        return comments;
    }

    @Transactional
    public CommentDTO updateComment(Long id, CommentDTO commentDTO, Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Update comment attempt by user: {}, commentId: {}", userEmail, id);

        User user = findUserByEmail(userEmail);

        Comment comment = findCommentById(id);

        validateCommentOwnership(comment, user.getId(), id);

        comment.setCommentText(commentDTO.getCommentText());
        Comment updated = commentRepository.save(comment);

        log.info("Comment updated successfully: commentId={}, user={}", id, userEmail);
        return commentMapper.toDto(updated);
    }

    @Transactional
    public void deleteComment(Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Delete comment attempt by user: {}, commentId: {}", userEmail, id);

        User user = findUserByEmail(userEmail);

        Comment comment = findCommentById(id);

        validateCommentOwnership(comment, user.getId(), id);

        commentRepository.delete(comment);
        log.info("Comment deleted successfully: commentId={}, user={}", id, userEmail);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getAllComments() {
        log.debug("Fetching all comments");
        List<CommentDTO> comments = commentRepository.findAll().stream()
                .map(commentMapper::toDto)
                .toList();
        log.debug("Found {} total comments", comments.size());
        return comments;
    }

    /**
     * Finds user by email or throws ResourceNotFoundException.
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found: {}", email);
                    return new ResourceNotFoundException(USER_NOT_FOUND);
                });
    }

    /**
     * Finds comment by ID or throws ResourceNotFoundException.
     */
    private Comment findCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Comment not found: commentId={}", id);
                    return new ResourceNotFoundException(COMMENT_NOT_FOUND + id);
                });
    }

    /**
     * Validates that the comment belongs to the specified user.
     * Throws AccessDeniedException if ownership check fails.
     */
    private void validateCommentOwnership(Comment comment, Long userId, Long commentId) {
        if (!comment.getUserId().equals(userId)) {
            log.warn("Unauthorized access: userId {} tried to modify commentId {}", userId, commentId);
            throw new AccessDeniedException(UNAUTHORIZED_MODIFY);
        }
    }
}
