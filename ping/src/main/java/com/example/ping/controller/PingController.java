package com.example.ping.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.time.Duration;

@RestController
@Slf4j
public class PingController {

    @GetMapping(value = "/ping")
    public Mono<String> ping() {
        try {
            RandomAccessFile file = new RandomAccessFile("/Users/zhongbo/Downloads/demo/eee.txt", "rw");
            RandomAccessFile file2 = new RandomAccessFile("/Users/zhongbo/Downloads/demo/www.txt", "rw");

            WebClient webClient = WebClient.create();

            Flux.interval(Duration.ofSeconds(1))
                    .subscribe(i -> {
                                FileLock lock = null;

                                try {
                                    lock = file.getChannel().tryLock();
                                    if (lock == null) {
                                        lock = file2.getChannel().tryLock();
                                    }
                                    if (lock == null) {
                                        log.info("get lock failure, rate limited");
                                    }

                                    Mono<String> responseMono = webClient.get()
                                            .uri("http://localhost:8081/pong/Hello")
                                            .retrieve()
                                            .bodyToMono(String.class);

                                    responseMono.subscribe(response -> {
                                                // 处理响应数据
                                                log.info("send success and Response: {}", response);
                                            },
                                            error -> {
                                                // 处理错误
                                                log.error("send success and Error: {}", error.getMessage());
                                            });
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                } finally {
                                    if (lock != null) {
                                        try {
                                            lock.release();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }


                            }

                    );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Mono.just("");
    }

}
