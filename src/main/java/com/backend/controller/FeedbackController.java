package com.backend.controller;

import com.backend.dto.FeedbackDTO;
import com.backend.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback Management")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "Create a new feedback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Feedback created successfully",
                    content = @Content(schema = @Schema(implementation = FeedbackDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<FeedbackDTO> createFeedback(
            @Valid @RequestBody FeedbackDTO feedbackDTO,
            Authentication authentication) {
        FeedbackDTO created = feedbackService.createFeedback(feedbackDTO, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get feedback by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback found",
                    content = @Content(schema = @Schema(implementation = FeedbackDTO.class))),
            @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDTO> getFeedbackById(@PathVariable Integer id) {
        return ResponseEntity.ok(feedbackService.getFeedbackById(id));
    }

    @Operation(summary = "Get all feedbacks by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of feedbacks for user",
                    content = @Content(schema = @Schema(implementation = FeedbackDTO.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbackByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByUserId(userId));
    }

    @Operation(summary = "Get all feedbacks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all feedbacks",
                    content = @Content(schema = @Schema(implementation = FeedbackDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<FeedbackDTO>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    @Operation(summary = "Update a feedback by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback updated successfully",
                    content = @Content(schema = @Schema(implementation = FeedbackDTO.class))),
            @ApiResponse(responseCode = "403", description = "User is not allowed to update this feedback"),
            @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackDTO> updateFeedback(
            @PathVariable Integer id,
            @Valid @RequestBody FeedbackDTO feedbackDTO,
            Authentication authentication) {
        return ResponseEntity.ok(feedbackService.updateFeedback(id, feedbackDTO, authentication));
    }

    @Operation(summary = "Delete a feedback by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Feedback deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to delete this feedback"),
            @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Integer id, Authentication authentication) {
        feedbackService.deleteFeedback(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
