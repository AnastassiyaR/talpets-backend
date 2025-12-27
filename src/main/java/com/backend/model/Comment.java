package com.backend.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "comment_text", nullable = false, length = 1000)
    private String commentText;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
