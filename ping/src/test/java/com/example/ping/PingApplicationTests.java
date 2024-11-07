package com.example.ping;

import com.example.ping.controller.PingController;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Duration;


@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PingController.class)
//@SpringBootTest
class PingApplicationTests {
    private static final Logger log = LoggerFactory.getLogger(PingApplicationTests.class);
    @Autowired
    private PingController pingController;

    @Autowired
    private WebTestClient webClient;

    @Test
    void normal() {
        StepVerifier.withVirtualTime(() -> {
                    webClient.get()
                            .uri("/ping")
                            .exchange()
                            .expectStatus().isOk();
                    return Mono.just("");
                })
                .thenAwait(Duration.ofSeconds(10))
                .expectNext("")
                .verifyComplete();
    }

    @Test
    void return429() throws IOException {
        StepVerifier.withVirtualTime(() -> {
                    return Mono.zip(
                            pingController.ping(),
                            pingController.ping(),
                            pingController.ping()
                    ).then(Mono.empty());
                })
                .thenAwait(Duration.ofSeconds(5))
                .verifyComplete();

    }

    @Test
    void fileLocked() throws IOException {
        RandomAccessFile file = new RandomAccessFile("/Users/zhongbo/Downloads/demo/eee.txt", "rw");

        try {
            FileLock lock = file.getChannel().tryLock();
            StepVerifier.withVirtualTime(() -> pingController.ping())
                    .thenAwait(Duration.ofSeconds(5))
                    .expectNext("")
                    .verifyComplete();
        } catch (Exception e) {
            log.info("success");
        }


    }

}
