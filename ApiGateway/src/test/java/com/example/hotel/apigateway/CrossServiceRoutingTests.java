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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
public class CrossServiceRoutingTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WireMockServer wireMockServer;

    private String baseUrl;
    
    // JWT token for authenticated requests
    private static final String VALID_JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZXMiOiJVU0VSIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

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
    public void testCreateBookingFlowAcrossServices() {
        // 1. First mock the event service endpoint for event details
        wireMockServer.stubFor(get(urlEqualTo("/api/events/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"name\":\"Conference\",\"seats\":100,\"availableSeats\":50}")));
        
        // 2. Then mock the booking service endpoint for creating a booking
        wireMockServer.stubFor(post(urlEqualTo("/api/bookings"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":101,\"eventId\":1,\"userId\":1,\"status\":\"PENDING\",\"reservationCode\":\"RES-123456\"}")));
        
        // 3. Finally mock the notification service endpoint
        wireMockServer.stubFor(post(urlEqualTo("/api/notifications"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":201,\"userId\":1,\"message\":\"Your booking has been confirmed.\",\"read\":false}")));
        
        // Create booking request
        String requestBody = "{\"eventId\":1,\"numberOfSeats\":2,\"specialRequirements\":\"Near the stage\"}";
        
        // Create headers with JWT for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_JWT_TOKEN);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        // Send request through API Gateway to create booking
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/bookings", 
                HttpMethod.POST, 
                request, 
                String.class);
        
        // Verify booking creation response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // Verify that the request was routed to the Booking service
        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/bookings"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + VALID_JWT_TOKEN))
                .withHeader("X-User-Id", equalTo("1"))
                .withHeader("X-User-Roles", equalTo("USER"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(requestBody)));
    }
    
    @Test
    public void testViewBookingDetailsAcrossServices() {
        // 1. Mock the booking service endpoint
        wireMockServer.stubFor(get(urlEqualTo("/api/bookings/101"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":101,\"eventId\":1,\"userId\":1,\"status\":\"CONFIRMED\",\"reservationCode\":\"RES-123456\"}")));
        
        // 2. Mock the event service endpoint that would be called to get event details
        wireMockServer.stubFor(get(urlEqualTo("/api/events/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"name\":\"Conference\",\"description\":\"Tech conference\",\"venue\":\"Convention Center\"}")));
        
        // Create headers with JWT for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_JWT_TOKEN);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        
        // Send request through API Gateway to view booking
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/bookings/101", 
                HttpMethod.GET, 
                request, 
                String.class);
        
        // Verify booking view response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that the request was routed to the Booking service
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/bookings/101"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + VALID_JWT_TOKEN))
                .withHeader("X-User-Id", equalTo("1"))
                .withHeader("X-User-Roles", equalTo("USER")));
    }
    
    @Test
    public void testCancelBookingAcrossServices() {
        // 1. Mock the booking service endpoint for cancellation
        wireMockServer.stubFor(delete(urlEqualTo("/api/bookings/101"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":101,\"eventId\":1,\"userId\":1,\"status\":\"CANCELLED\"}")));
        
        // 2. Mock the notification service that would be called after cancellation
        wireMockServer.stubFor(post(urlEqualTo("/api/notifications"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":202,\"userId\":1,\"message\":\"Your booking has been cancelled.\",\"read\":false}")));
        
        // Create headers with JWT for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_JWT_TOKEN);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        
        // Send request through API Gateway to cancel booking
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/bookings/101", 
                HttpMethod.DELETE, 
                request, 
                String.class);
        
        // Verify cancellation response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that the request was routed to the Booking service
        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/api/bookings/101"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + VALID_JWT_TOKEN))
                .withHeader("X-User-Id", equalTo("1"))
                .withHeader("X-User-Roles", equalTo("USER")));
    }
    
    @Test
    public void testSearchEventsThenBookingFlow() {
        // 1. Mock the search service endpoint
        wireMockServer.stubFor(get(urlPathMatching("/api/events"))
                .withQueryParam("query", equalTo("conference"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":1,\"name\":\"Tech Conference\",\"description\":\"Annual tech event\"}]")));
        
        // 2. Mock the event details endpoint
        wireMockServer.stubFor(get(urlEqualTo("/api/events/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"name\":\"Tech Conference\",\"description\":\"Annual tech event\",\"seats\":100,\"availableSeats\":50}")));
        
        // 3. Mock the booking creation endpoint
        wireMockServer.stubFor(post(urlEqualTo("/api/bookings"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":102,\"eventId\":1,\"userId\":1,\"status\":\"CONFIRMED\",\"reservationCode\":\"RES-654321\"}")));
        
        // First search for events
        ResponseEntity<String> searchResponse = restTemplate.getForEntity(
                baseUrl + "/api/events?query=conference", 
                String.class);
        
        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        
        // Verify search request was routed to the Event service
        wireMockServer.verify(getRequestedFor(urlPathMatching("/api/events"))
                .withQueryParam("query", equalTo("conference")));
        
        // Then create a booking
        String requestBody = "{\"eventId\":1,\"numberOfSeats\":2}";
        
        // Create headers with JWT for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_JWT_TOKEN);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        // Send request through API Gateway to create booking
        ResponseEntity<String> bookingResponse = restTemplate.exchange(
                baseUrl + "/api/bookings", 
                HttpMethod.POST, 
                request, 
                String.class);
        
        // Verify booking creation response
        assertEquals(HttpStatus.CREATED, bookingResponse.getStatusCode());
        
        // Verify that the request was routed to the Booking service
        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/bookings"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(requestBody)));
    }
} 