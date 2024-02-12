package com.practice.demo.controller;

import com.practice.demo.service.RedisPublisher;
import com.practice.demo.service.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//참고 https://zkdlu.github.io/2020-12-29/redis04-spring-boot%EC%97%90%EC%84%9C-pub,sub-%EB%AA%A8%EB%8D%B8-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0/

@RequestMapping("/api/pubsub/topics")
@RequiredArgsConstructor
@RestController
public class PubSubController {
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final RedisPublisher redisPublisher;
    private final RedisSubscriber redisSubscriber;

    private static Map<String, ChannelTopic> channels = new ConcurrentHashMap<>();

    //토픽 목록
    @GetMapping
    public Set<String> getTopicAll() {
        return channels.keySet();
    }

    //토픽 생성
    @PutMapping("/{name}")
    public void createTopic(@PathVariable String name){
        ChannelTopic channelTopic = new ChannelTopic(name);
        redisMessageListenerContainer.addMessageListener(redisSubscriber, channelTopic);
        channels.put(name, channelTopic);
    }

    //메시지 발행
    @PostMapping("/{name}")
    public void pushMessage(@PathVariable String name, @RequestParam String message){
        ChannelTopic channelTopic = channels.get(name);
        redisPublisher.publish(channelTopic, message);
    }

    //토픽제거
    @DeleteMapping("/{name}")
    public void deleteTopic(@PathVariable String name){
        ChannelTopic channelTopic = channels.get(name);
        redisMessageListenerContainer.removeMessageListener(redisSubscriber, channelTopic);
        channels.remove(name);
    }

}
