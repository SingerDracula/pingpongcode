package com.example.ping.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.time.Duration;

@RestController
@Slf4j
public class PingController {
    public void setFile(RandomAccessFile file) {
        this.file = file;
    }

    public void setFile2(RandomAccessFile file2) {
        this.file2 = file2;
    }

    public void setMax(int max) {
        this.max = max;
    }

    private RandomAccessFile file;
    private RandomAccessFile file2;
    private int max = 100;

    @GetMapping(value = "/ping")
    public Mono<String> ping() {
        try {
            if (file == null || file2 == null) {
                file = new RandomAccessFile("/Users/zhongbo/Downloads/demo/eee.txt", "rw");
                file2 = new RandomAccessFile("/Users/zhongbo/Downloads/demo/www.txt", "rw");
            }

            WebClient webClient = WebClient.create();

            Flux.interval(Duration.ofSeconds(1))
                    .takeWhile(num -> num < max)
                    .subscribe(i -> {
                                FileLock lock = null;

                                try {
                                    lock = file.getChannel().tryLock();
                                    if (lock == null) {
                                        lock = file2.getChannel().tryLock();
                                    }
                                    if (lock == null) {
                                        log.info("get lock failure, rate limited");
                                        return;
                                    }

                                    Mono<String> responseMono = webClient.get()
                                            .uri("http://127.0.0.1:8081/pong/Hello4")
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
                                            Thread.sleep(500);
                                            lock.release();
                                        } catch (Exception e) {
                                            log.error("Error releasing lock: " + e.getMessage());
                                        }
                                    }
                                }


                            }

                    );
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
        }
        log.info("完成");
        return Mono.just("");
    }

}
