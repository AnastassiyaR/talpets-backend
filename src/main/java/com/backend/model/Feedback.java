package com.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "feedback_text", nullable = false, columnDefinition = "TEXT")
    private String feedbackText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}