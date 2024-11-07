package com.example.pong;

import com.example.pong.controller.PongController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PongController.class)
class PongApplicationTests {

	@Autowired
	private PongController pongController;

	@Autowired
	private WebTestClient webClient;
	@Test
	void normal() {
		StepVerifier.withVirtualTime(() -> {
					webClient.get()
							.uri("/pong/ping")
							.exchange()
							.expectStatus().isOk();
					return Mono.just("");
				})
				.thenAwait(Duration.ofSeconds(10))
				.expectNext("")
				.verifyComplete();
	}

}
