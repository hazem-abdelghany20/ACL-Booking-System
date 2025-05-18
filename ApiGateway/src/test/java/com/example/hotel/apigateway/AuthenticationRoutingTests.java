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
public class AuthenticationRoutingTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WireMockServer wireMockServer;

    private String baseUrl;
    
    // JWT token for authenticated requests
    private static final String VALID_JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZXMiOiJVU0VSIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String INVALID_JWT_TOKEN = "invalid.token.format";

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
        registry.add("spring.cloud.gateway.routes[4].uri", 
                () -> "http://localhost:${wiremock.server.port}");
        registry.add("spring.cloud.gateway.routes[5].uri", 
                () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void testSignInSuccess() {
        // Setup mock for UserAuth service
        wireMockServer.stubFor(post(urlEqualTo("/api/auth/signin"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"accessToken\":\"" + VALID_JWT_TOKEN + "\",\"username\":\"testuser\",\"roles\":[\"USER\"]}")));

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
    public void testSecuredEndpointWithValidToken() {
        // Setup mock for UserAuth service
        wireMockServer.stubFor(get(urlEqualTo("/api/users/profile"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"username\":\"testuser\",\"email\":\"test@example.com\"}")));

        // Create headers with a valid JWT token
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_JWT_TOKEN);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        
        // Send request through API Gateway
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/users/profile", 
                HttpMethod.GET, 
                request, 
                String.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that the request was routed to the UserAuth service with the JWT token
        // and that user information was added to the headers
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/users/profile"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + VALID_JWT_TOKEN))
                .withHeader("X-User-Id", equalTo("1"))
                .withHeader("X-User-Roles", equalTo("USER")));
    }
    
    @Test
    public void testSecuredEndpointWithInvalidToken() {
        // Setup mock for UserAuth service - this should not be called as the Gateway should reject the request
        wireMockServer.stubFor(get(urlEqualTo("/api/users/profile"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"username\":\"testuser\",\"email\":\"test@example.com\"}")));

        // Create headers with an invalid JWT token
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_JWT_TOKEN);
        
        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        
        // Send request through API Gateway
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/users/profile", 
                HttpMethod.GET, 
                request, 
                String.class);
        
        // Verify that the API Gateway rejected the request with 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        // Verify that no request was sent to the UserAuth service
        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/api/users/profile")));
    }
    
    @Test
    public void testSecuredEndpointWithNoToken() {
        // Setup mock for UserAuth service - this should not be called as the Gateway should reject the request
        wireMockServer.stubFor(get(urlEqualTo("/api/users/profile"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"username\":\"testuser\",\"email\":\"test@example.com\"}")));

        // Create request with no Authorization header
        HttpEntity<String> request = new HttpEntity<>(null, new HttpHeaders());
        
        // Send request through API Gateway
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/users/profile", 
                HttpMethod.GET, 
                request, 
                String.class);
        
        // Verify that the API Gateway rejected the request with 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        // Verify that no request was sent to the UserAuth service
        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/api/users/profile")));
    }
    
    @Test
    public void testPublicEndpointNoAuthentication() {
        // Setup mock for public endpoint that doesn't require auth
        wireMockServer.stubFor(get(urlEqualTo("/api/events/public"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":1,\"name\":\"Public Event\",\"description\":\"Everyone can see this\"}]")));

        // Send request without authentication
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/events/public", 
                String.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that the request was routed to the Event service
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/events/public")));
    }
    
    @Test
    public void testOAuthRedirectRouting() {
        // Setup mock for OAuth callback endpoint
        wireMockServer.stubFor(get(urlEqualTo("/api/oauth/callback?code=test-code"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", "/dashboard")));

        // Send request to OAuth callback endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/oauth/callback?code=test-code", 
                String.class);
        
        // Verify response - should be a redirect
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        
        // Verify that the request was routed to the Auth service
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/oauth/callback?code=test-code")));
    }
} 