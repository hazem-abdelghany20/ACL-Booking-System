package com.example.hotel.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
public class RoutingIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WireMockServer wireMockServer;

    private String baseUrl;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port;
        
        // Reset WireMock before each test
        wireMockServer.resetAll();
    }
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Override the service URLs to point to WireMock server
        registry.add("spring.cloud.gateway.routes[0].uri", 
                () -> "http://localhost:${wiremock.server.port}");
        registry.add("spring.cloud.gateway.routes[1].uri", 
                () -> "http://localhost:${wiremock.server.port}");
        registry.add("spring.cloud.gateway.routes[2].uri", 
                () -> "http://localhost:${wiremock.server.port}");
        registry.add("spring.cloud.gateway.routes[3].uri", 
                () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void testSignInRoutingToUserAuthService() {
        // Setup mock for UserAuth service
        wireMockServer.stubFor(post(urlEqualTo("/api/auth/signin"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\":\"test-jwt-token\",\"username\":\"testuser\"}")));

        // Create request body
        String requestBody = "{\"username\":\"testuser\",\"password\":\"password\"}";
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        // Send request through API Gateway
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/auth/signin", 
                HttpMethod.POST, 
                request, 
                String.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that the request was routed to the UserAuth service
        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/auth/signin"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(requestBody)));
    }
    
    @Test
    public void testSignUpRoutingToUserAuthService() {
        // Setup mock for UserAuth service
        wireMockServer.stubFor(post(urlEqualTo("/api/auth/signup"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"User registered successfully\"}")));

        // Create request body
        String requestBody = "{\"username\":\"newuser\",\"email\":\"newuser@example.com\",\"password\":\"password\"}";
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        // Send request through API Gateway
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/auth/signup", 
                HttpMethod.POST, 
                request, 
                String.class);
        
        // Verify response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // Verify that the request was routed to the UserAuth service
        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/auth/signup"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(requestBody)));
    }
    
    @Test
    public void testEventServiceRouting() {
        // Setup mock for Event service
        wireMockServer.stubFor(get(urlEqualTo("/api/events"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":1,\"name\":\"Conference\",\"description\":\"Tech conference\"}]")));
        
        // Send request through API Gateway
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/events", 
                String.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that the request was routed to the Event service
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/events")));
    }
    
    @Test
    public void testBookingServiceRouting() {
        // Setup mock token for authentication
        String jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZXMiOiJVU0VSIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        // Setup mock for Booking service
        wireMockServer.stubFor(get(urlEqualTo("/api/bookings"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":1,\"eventId\":1,\"userId\":1,\"status\":\"CONFIRMED\"}]")));
        
        // Create headers with authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        
        // Send request through API Gateway
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/bookings", 
                HttpMethod.GET, 
                request, 
                String.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that the request was routed to the Booking service with user info headers
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/bookings"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + jwtToken))
                .withHeader("X-User-Id", equalTo("1"))
                .withHeader("X-User-Roles", equalTo("USER")));
    }
    
    @Test
    public void testNotificationServiceRouting() {
        // Setup mock token for authentication
        String jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZXMiOiJVU0VSIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        // Setup mock for Notification service
        wireMockServer.stubFor(get(urlEqualTo("/api/notifications"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":1,\"userId\":1,\"message\":\"Booking confirmed\",\"read\":false}]")));
        
        // Create headers with authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        
        // Send request through API Gateway
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/notifications", 
                HttpMethod.GET, 
                request, 
                String.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that the request was routed to the Notification service with user info headers
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/notifications"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + jwtToken))
                .withHeader("X-User-Id", equalTo("1"))
                .withHeader("X-User-Roles", equalTo("USER")));
    }
} 