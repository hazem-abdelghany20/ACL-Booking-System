package com.example.hotel.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@Import({TestConfig.class, WireMockConfig.class})
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
        
        // Setup stubs for booking service
        wireMockServer.stubFor(post(urlEqualTo("/api/bookings"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":101,\"eventId\":1,\"userId\":1,\"numberOfSeats\":2,\"status\":\"PENDING\",\"specialRequirements\":\"Near the stage\"}")));
        
        wireMockServer.stubFor(get(urlEqualTo("/api/bookings/101"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":101,\"eventId\":1,\"userId\":1,\"numberOfSeats\":2,\"status\":\"PENDING\",\"specialRequirements\":\"Near the stage\"}")));
        
        wireMockServer.stubFor(delete(urlEqualTo("/api/bookings/101"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Booking cancelled successfully\"}")));
        
        // Setup stubs for event service
        wireMockServer.stubFor(get(urlEqualTo("/api/events?query=conference"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":1,\"name\":\"Tech Conference\",\"description\":\"Annual tech conference\",\"capacity\":500,\"date\":\"2023-12-15\"}]")));
        
        wireMockServer.stubFor(get(urlEqualTo("/api/events/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"name\":\"Tech Conference\",\"description\":\"Annual tech conference\",\"capacity\":500,\"date\":\"2023-12-15\"}")));
    }
    
    // Routes are now configured through TestConfig

    @Test
    public void testCreateBookingFlowAcrossServices() {
        // Create booking request body
        String requestBody = "{\"eventId\":1,\"numberOfSeats\":2,\"specialRequirements\":\"Near the stage\"}";
        
        // Create headers with JWT token for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_JWT_TOKEN);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        // Send POST request to create booking
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/bookings",
                HttpMethod.POST,
                request,
                String.class);
        
        // Verify response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // Verify that the request was routed to the Booking service
        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/bookings"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(requestBody)));
        
        // Verify response contains booking information
        assertTrue(response.getBody().contains("\"id\":101"));
        assertTrue(response.getBody().contains("\"eventId\":1"));
    }
    
    @Test
    public void testViewBookingDetailsAcrossServices() {
        // Create headers with JWT token for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_JWT_TOKEN);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        
        // Send GET request to view booking details
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/bookings/101",
                HttpMethod.GET,
                request,
                String.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that the request was routed to the Booking service
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/bookings/101"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + VALID_JWT_TOKEN)));
        
        // Verify response contains booking information
        assertTrue(response.getBody().contains("\"id\":101"));
        assertTrue(response.getBody().contains("\"eventId\":1"));
    }
    
    @Test
    public void testCancelBookingAcrossServices() {
        // Create headers with JWT token for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_JWT_TOKEN);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        
        // Send DELETE request to cancel booking
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/bookings/101",
                HttpMethod.DELETE,
                request,
                String.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that the request was routed to the Booking service
        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/api/bookings/101"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + VALID_JWT_TOKEN)));
        
        // Verify response contains success message
        assertTrue(response.getBody().contains("\"message\":\"Booking cancelled successfully\""));
    }
    
    @Test
    public void testSearchEventsThenBookingFlow() {
        // Create headers with JWT token for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_JWT_TOKEN);
        
        // Send GET request to search events
        ResponseEntity<String> searchResponse = restTemplate.exchange(
                baseUrl + "/api/events?query=conference",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class);
        
        // Verify search response
        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        
        // Verify that the request was routed to the Event service
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/events?query=conference"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + VALID_JWT_TOKEN)));
        
        // Create booking request body
        String requestBody = "{\"eventId\":1,\"numberOfSeats\":2,\"specialRequirements\":\"Near the stage\"}";
        
        // Update headers with content type for JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        // Send POST request to create booking
        ResponseEntity<String> bookingResponse = restTemplate.exchange(
                baseUrl + "/api/bookings",
                HttpMethod.POST,
                request,
                String.class);
        
        // Verify booking response
        assertEquals(HttpStatus.CREATED, bookingResponse.getStatusCode());
        
        // Verify that the request was routed to the Booking service
        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/bookings"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(requestBody)));
    }
} 