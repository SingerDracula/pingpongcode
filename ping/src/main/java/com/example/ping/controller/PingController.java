package com.example.ping.controller;

import com.example.ping.RedisLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private int max = 10000;

    @Autowired
    private RedisLockUtil redisLockUtil;

    @GetMapping(value = "/ping")
    public Mono<String> ping() {
        try {
            WebClient webClient = WebClient.create();

            //每秒发送 1 个请求
            Flux.interval(Duration.ofSeconds(1))
                    //限制发送邹总数
                    .takeWhile(num -> num < max)
                    .subscribe(i -> {
                                FileLock lock = null;

                                try {

                                    //redis 实现滑动窗口计数器，控制请求 rate
                                    if (!redisLockUtil.isAllowed()){
                                        System.out.println("rate limited");
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
