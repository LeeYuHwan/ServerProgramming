package com.practice.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class LongPollingExController {
    //참고 https://junhyunny.github.io/information/spring-boot/polling-long-polling-and-spring-example/

    private Map<String, DeferredResult<Boolean>> authRequests = new ConcurrentHashMap<>();

    @GetMapping("/pool-size")
    public int poolSize(){
        return authRequests.size();
    }

    @GetMapping("/auth")
    public DeferredResult<Boolean> requestAuth(@RequestParam("order") String order){
        DeferredResult<Boolean> deferredResult = new DeferredResult<>();

        deferredResult.onTimeout(() -> {
            authRequests.remove(order);
        });

        deferredResult.onCompletion(() -> {
            authRequests.remove(order);
        });

        deferredResult.onError((throwable) -> {
            deferredResult.setResult(false);
            authRequests.remove(order);
        });

        authRequests.put(order, deferredResult);
        return deferredResult;
    }

    @PostMapping("/auth")
    public void auth(@RequestParam("order") String order){
        DeferredResult authRequest = authRequests.remove(order);

        if (authRequest == null) return;

        authRequest.setResult(true);
    }
}
