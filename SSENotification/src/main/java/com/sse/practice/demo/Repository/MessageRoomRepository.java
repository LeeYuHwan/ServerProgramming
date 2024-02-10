package com.sse.practice.demo.Repository;

import com.sse.practice.demo.domain.MessageRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRoomRepository extends JpaRepository<MessageRoom, Long> {
    MessageRoom findByRoomId(Long id);
}
