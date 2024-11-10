package com.example.ping;

import com.example.ping.controller.PingController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Duration;

import static org.mockito.Mockito.when;


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
    void fileGetLockNull() throws IOException {
        RandomAccessFile file2 = new RandomAccessFile("/Users/zhongbo/Downloads/demo/eee.txt", "rw");
        RandomAccessFile file = Mockito.mock(RandomAccessFile.class);
        FileChannel mockChannel = Mockito.mock(FileChannel.class);
        when(file.getChannel()).thenReturn(mockChannel);
        when(mockChannel.tryLock()).thenReturn(null);
        pingController.setFile(file);
        pingController.setFile2(file2);
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
    void fileAndFile2GetLockNull() throws IOException {
        RandomAccessFile file2 = Mockito.mock(RandomAccessFile.class);;
        RandomAccessFile file = Mockito.mock(RandomAccessFile.class);
        FileChannel mockChannel = Mockito.mock(FileChannel.class);
        when(file.getChannel()).thenReturn(mockChannel);
        when(file2.getChannel()).thenReturn(mockChannel);
        when(mockChannel.tryLock()).thenReturn(null);
        pingController.setFile(file);
        pingController.setFile2(file2);
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
