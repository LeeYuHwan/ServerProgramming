package com.sse.practice.demo.repository;

import com.sse.practice.demo.domain.MessageRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRoomRepository extends JpaRepository<MessageRoom, Long> {
    MessageRoom findByRoomId(Long id);
}
