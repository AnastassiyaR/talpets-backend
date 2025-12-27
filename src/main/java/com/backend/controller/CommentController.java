package com.backend.controller;


import com.backend.dto.CommentDTO;
import com.backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "API for managing product comments")
@SecurityRequirement(name = "Bearer Authentication")
public class CommentController {

    private final CommentService commentService;


    @Operation(summary = "Get all comments for a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(commentService.getCommentsByProductId(productId));
    }


    @Operation(summary = "Create a new comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(
            @Valid @RequestBody CommentDTO commentDTO,
            Authentication authentication
    ) {
        CommentDTO created = commentService.createComment(commentDTO, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @Operation(summary = "Update existing comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not allowed to update this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentDTO commentDTO,
            Authentication authentication
    ) {
        CommentDTO updated = commentService.updateComment(id, commentDTO, authentication);
        return ResponseEntity.ok(updated);
    }


    @Operation(summary = "Delete comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not allowed to delete this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        commentService.deleteComment(id, authentication);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Get all comments from all products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<CommentDTO>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }


    @Operation(summary = "Get comment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }


    @Operation(summary = "Get all comments by specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(commentService.getCommentsByUserId(userId));
    }
}
