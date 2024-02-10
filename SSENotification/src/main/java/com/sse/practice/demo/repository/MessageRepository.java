package com.sse.practice.demo.repository;

import com.sse.practice.demo.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findFirstBySenderOrderByCreatedTimeDesc(String id);
}
