package com.sse.practice.demo.controller;

import com.sse.practice.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

//참고 https://velog.io/@baekgom/SSE-Server-Sent-Event-%EC%95%8C%EB%A6%BC-%EA%B8%B0%EB%8A%A5

@RestController
@RequiredArgsConstructor
public class SSENotificationController {
    private final NotificationService notificationService;

    public SseEmitter subscribe(@PathVariable Long id){
        return notificationService.subscriber(id);
    }

    @PostMapping("/api/notification/comment/{postId}")
    public boolean createComment(@PathVariable Long postId) {
        return notificationService.notifyComment(postId);
    }

    @DeleteMapping("/api/notification/delete/{id}")
    public boolean deleteNotification(@PathVariable Long id) throws IOException {
        return notificationService.deleteNotification(id);
    }

}
