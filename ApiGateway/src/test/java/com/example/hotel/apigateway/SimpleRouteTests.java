package com.example.hotel.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
public class SimpleRouteTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void contextLoads() {
        // Simple test to verify Spring context loads
    }

    @Test
    public void actuatorHealthEndpointIsAccessible() {
        webTestClient
            .get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk(); // Redis should now be working
    }
} 