package com.example.ping

import com.example.ping.controller.PingController
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.channels.FileChannel
import java.nio.channels.OverlappingFileLockException

import static org.mockito.Mockito.when

@SpringBootTest
@ExtendWith(SpringExtension.class)
class PingTestsSpec extends Specification {

    @Autowired
    private PingController pingController;

    @Unroll
    def "normal"() {
        given: "ready"
        pingController.setMax(2)

        when: "call"
        Mono<String> response = pingController.ping()

        then: "use StepVerifier"
        Thread.sleep(3000)
        StepVerifier.create(response)
                .expectNext("")
                .verifyComplete()

    }

    @Unroll
    def "fileGetLockNull"() {
        given: "ready"
        pingController.setMax(2)
        RandomAccessFile file2 = new RandomAccessFile("/Users/zhongbo/Downloads/demo/eee.txt", "rw");
        RandomAccessFile file = Mockito.mock(RandomAccessFile.class);
        FileChannel mockChannel = Mockito.mock(FileChannel.class);
        when(file.getChannel()).thenReturn(mockChannel);
        when(mockChannel.tryLock()).thenReturn(null);
        pingController.setFile(file);
        pingController.setFile2(file2);

        when: "call"
        Mono<String> response = pingController.ping()

        then: "use StepVerifier"
        Thread.sleep(2000)
        StepVerifier.create(response)
                .expectNext("")
                .verifyComplete()
    }

    @Unroll
    def "fileAndFile2GetLockNull"() {
        given: "ready"
        pingController.setMax(2)
        RandomAccessFile file2 = Mockito.mock(RandomAccessFile.class); ;
        RandomAccessFile file = Mockito.mock(RandomAccessFile.class);
        FileChannel mockChannel = Mockito.mock(FileChannel.class);
        when(file.getChannel()).thenReturn(mockChannel);
        when(file2.getChannel()).thenReturn(mockChannel);
        when(mockChannel.tryLock()).thenReturn(null);
        pingController.setFile(file);
        pingController.setFile2(file2);

        when: "call"
        Mono<String> response = pingController.ping()

        then: "use StepVerifier"
        Thread.sleep(2000)
        StepVerifier.create(response)
                .expectNext("")
                .verifyComplete()
    }

    @Unroll
    def "fileLocked"() {
        given: "ready"
        pingController.setMax(2)
        RandomAccessFile file2 = Mockito.mock(RandomAccessFile.class); ;
        RandomAccessFile file = Mockito.mock(RandomAccessFile.class);
        FileChannel mockChannel = Mockito.mock(FileChannel.class);
        when(file.getChannel()).thenThrow(OverlappingFileLockException);
        when(file2.getChannel()).thenReturn(mockChannel);
        when(mockChannel.tryLock()).thenReturn(null);
        pingController.setFile(file);
        pingController.setFile2(file2);

        when: "call"
        Mono<String> response = pingController.ping()

        then: "use StepVerifier"
        Thread.sleep(2000)
        StepVerifier.create(response)
                .expectNext("")
                .verifyComplete()
    }
}