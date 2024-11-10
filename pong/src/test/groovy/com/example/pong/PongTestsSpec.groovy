package com.example.pong

import com.example.pong.controller.PongController
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration

@SpringBootTest
@ExtendWith(SpringExtension.class)
class PongTestsSpec extends Specification {

    @Autowired
    private PongController pongController;

    @Unroll
    def "normal"() {
        given: "ready"

        when: "call"
        Mono<ResponseEntity<String>> response = pongController.pong("ping")

        then: "use StepVerifier"
        StepVerifier.create(response)
                .thenAwait(Duration.ofSeconds(10))
                .expectNextMatches {
                    it.statusCode == HttpStatus.OK && it.body == "World"
                }
                .verifyComplete()

    }

    @Unroll
    def "return429"() {
        given: "ready"

        when: "call"
        def time = StepVerifier.withVirtualTime(() -> {
            return Mono.zip(
                    pongController.pong("ping"),
                    pongController.pong("ping"),
                    pongController.pong("ping")
            ).then(Mono.empty());
        })

        then: "use StepVerifier"
        time
                .thenAwait(Duration.ofSeconds(10))
                .verifyComplete()

    }
}