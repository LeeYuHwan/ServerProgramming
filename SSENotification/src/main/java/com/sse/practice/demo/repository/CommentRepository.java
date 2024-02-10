package com.sse.practice.demo.repository;

import com.sse.practice.demo.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findFirstByIdOrderByCreatedTimeDesc(Long id);
}
