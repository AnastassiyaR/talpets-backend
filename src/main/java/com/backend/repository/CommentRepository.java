package com.backend.repository;


import com.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByProductId(Long productId);

    List<Comment> findByUserId(Long userId);
}
