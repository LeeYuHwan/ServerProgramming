package com.sse.practice.demo.repository;

import com.sse.practice.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByNickname(String id);
}
