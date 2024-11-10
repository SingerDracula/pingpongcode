package com.example.pong.controller;

import com.example.pong.Counter.SlidingWindowLogCounter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class PongController {
    final SlidingWindowLogCounter slidingWindowLogCounter = new SlidingWindowLogCounter(1, 1000);
    @GetMapping(value = "pong/{data}")
    public Mono<ResponseEntity<String>> pong(@PathVariable String data) {
        log.info(data);
        if (slidingWindowLogCounter.allowRequest()) {
            log.error(data + "request received and return");
            return Mono.just(ResponseEntity.ok("World"));
        } else {
            log.error(data + "request received but not processed");
            return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
        }

    }

}
