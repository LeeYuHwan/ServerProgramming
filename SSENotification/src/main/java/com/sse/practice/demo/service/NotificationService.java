package com.sse.practice.demo.service;

import com.sse.practice.demo.Repository.*;
import com.sse.practice.demo.controller.SSENotificationController;
import com.sse.practice.demo.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

//참고1 https://velog.io/@baekgom/SSE-Server-Sent-Event-%EC%95%8C%EB%A6%BC-%EA%B8%B0%EB%8A%A5
//참고2 https://tecoble.techcourse.co.kr/post/2022-10-11-server-sent-events/

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final CommentRepository commentRepository;
    private final MessageRepository messageRepository;
    private final MessageRoomRepository messageRoomRepository;
    private final AtomicLong counter = new AtomicLong();
    private static Map<Long, Integer> notificationCounts = new HashMap<>();
    public static Map<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();


    // 메시지 알림
    public SseEmitter subscriber(Long userId) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);

        // 현재 클라이언트를 위한 sseEmitter 생성
        try {
            // 연결
            sseEmitter.send(SseEmitter.event().name("connect"));
        } catch (IOException e){
            log.error("SSE CONNECT EXCEPTION", e);
        }

        sseEmitterMap.put(userId, sseEmitter);

        sseEmitter.onCompletion(() -> sseEmitterMap.remove(userId));
        sseEmitter.onTimeout(() -> sseEmitterMap.remove(userId));
        sseEmitter.onError((e) -> sseEmitterMap.remove(userId));

        return sseEmitter;
    }

    // 메시지 알림 - receiver 에게
    public void notifyMessage(long roomId, String receiver, String sender) {
        MessageRoom messageRoom = messageRoomRepository.findByRoomId(roomId);

        Post post = postRepository.findById(messageRoom.getPost().getId()).orElseThrow(
                () -> new IllegalArgumentException("게시글을 찾을 수 없습니다.")
        );

        User user = userRepository.findByNickname(receiver);

        User userSender = userRepository.findByNickname(sender);

        Message receiveMessage = messageRepository.findFirstBySenderOrderByCreatedTimeDesc(userSender.getNickName()).orElseThrow(
                () -> new IllegalArgumentException("메시지를 찾을 수 없습니다.")
        );

        Long userId = user.getId();

        if (sseEmitterMap.containsKey(userId)) {
            SseEmitter sseEmitter = sseEmitterMap.get(userId);
            try {
                Map<String, String> eventData = new HashMap<>();
                eventData.put("message", "메시지가 왔습니다.");
                eventData.put("sender", receiveMessage.getSender());                    // 메시지 보낸자
                eventData.put("createdAt", receiveMessage.getCreateTime().toString());   // 메시지를 보낸 시간
                eventData.put("contents", receiveMessage.getMessage());                 // 메시지 내용

                sseEmitter.send(SseEmitter.event().name("addMessage").data(eventData));

                // DB 저장
                Notification notification = new Notification();
                notification.setSender(receiveMessage.getSender());
                notification.setCreatedTime(receiveMessage.getCreateTime());
                notification.setContent(receiveMessage.getMessage());
                notification.setRoomId(messageRoom.getRoomId());
                notification.setPost(post);         // post 필드 설정
                notificationRepository.save(notification);

                // 알림 개수 증가
                notificationCounts.put(userId, notificationCounts.getOrDefault(userId, 0) + 1);

                // 현재 알림 개수 전송
                sseEmitter.send(SseEmitter.event().name("notificationCount").data(notificationCounts.get(userId)));

            } catch (Exception e) {
                log.error("NOTIFY MESSAGE EXCEPTION");
                sseEmitterMap.remove(userId);
            }
        }
    }

    // 댓글 알림 - 게시글 작성자 에게
    public boolean notifyComment(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("게시글을 찾을 수 없습니다.")
        );

        Comment receiveComment = commentRepository.findFirstByIdOrderByCreatedTimeDesc(post.getId()).orElseThrow(
                () -> new IllegalArgumentException("댓글을 찾을 수 없습니다.")
        );

        Long userId = post.getUser().getId();

        if (sseEmitterMap.containsKey(userId)) {
            SseEmitter sseEmitter = sseEmitterMap.get(userId);
            try {
                Map<String, String> eventData = new HashMap<>();
                eventData.put("message", "댓글이 달렸습니다.");
                eventData.put("sender", receiveComment.getUser().getNickName());        // 댓글 작성자
                eventData.put("createdAt", receiveComment.getCreateTime().toString());   // 댓글이 달린 시간
                eventData.put("contents", receiveComment.getComment());                 // 댓글 내용

                sseEmitter.send(SseEmitter.event().name("addComment").data(eventData));

                // DB 저장
                Notification notification = new Notification();
                notification.setSender(receiveComment.getUser().getNickName());
                notification.setCreatedTime(receiveComment.getCreateTime());
                notification.setContent(receiveComment.getComment());
                notification.setPost(post);         // post 필드 설정
                notificationRepository.save(notification);

                // 알림 개수 증가
                notificationCounts.put(userId, notificationCounts.getOrDefault(userId, 0) + 1);

                // 현재 알림 개수 전송
                sseEmitter.send(SseEmitter.event().name("notificationCount").data(notificationCounts.get(userId)));

            } catch (IOException e) {
                log.error("NOTIFY COMMENT EXCEPTION");
                sseEmitterMap.remove(userId);
            }
        }

        return true;
    }

    // 알림 삭제
    public boolean deleteNotification(Long id) throws IOException {
        Notification notification = notificationRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("알림을 찾을 수 없습니다.")
        );

        Long userId = notification.getPost().getUser().getId();

        notificationRepository.delete(notification);

        // 알림 개수 감소
        if (notificationCounts.containsKey(userId)) {
            int currentCount = notificationCounts.get(userId);
            if (currentCount > 0) {
                notificationCounts.put(userId, currentCount - 1);
            }
        }

        // 현재 알림 개수 전송
        SseEmitter sseEmitter = sseEmitterMap.get(userId);
        sseEmitter.send(SseEmitter.event().name("notificationCount").data(notificationCounts.get(userId)));

        return true;
    }

    public void sendAllCountNotification() {
        long count = counter.incrementAndGet();
        sseEmitterMap.forEach((key, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("allCountNotification")
                        .data(count));
            } catch (IOException e) {
                log.error("ALL COUNT NOTIFY EXCEPTION");
            }
        });
    }

}
