package com.example.hotel.UserAuthService.config;

import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@Profile("test")
public class TestConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    @Primary
    public WebClient supabaseClient() {
        // Create a mock WebClient
        WebClient mockWebClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec mockHeadersSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec mockRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec mockResponseSpec = mock(WebClient.ResponseSpec.class);
        
        // Setup the chain for GET requests
        when(mockWebClient.get()).thenReturn(mockHeadersSpec);
        when(mockHeadersSpec.uri(anyString())).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        
        // Setup for POST requests
        WebClient.RequestBodyUriSpec mockBodySpec = mock(WebClient.RequestBodyUriSpec.class);
        when(mockWebClient.post()).thenReturn(mockBodySpec);
        when(mockBodySpec.uri(anyString())).thenReturn(mockBodySpec);
        when(mockBodySpec.header(anyString(), anyString())).thenReturn(mockBodySpec);
        when(mockBodySpec.bodyValue(any())).thenReturn(mockRequestHeadersSpec);
        
        // Mock responses for different operations
        
        // Mock OAuth providers response
        Map<String, Object> providersResponse = new HashMap<>();
        providersResponse.put("google", Map.of("enabled", true));
        providersResponse.put("facebook", Map.of("enabled", true));
        
        // Mock Google sign-in URL response
        Map<String, String> signInUrlResponse = new HashMap<>();
        signInUrlResponse.put("url", "https://mock-supabase-url.com/auth/v1/authorize?provider=google&redirect_to=http://localhost:8081/api/oauth/callback");
        
        // Mock Auth response
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("mock-access-token");
        authResponse.setRefreshToken("mock-refresh-token");
        
        // Create and set user data with ID
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", "user-123");
        authResponse.setUser(userData);
        
        // Configure responses
        when(mockResponseSpec.bodyToMono(Map.class))
            .thenReturn(Mono.just(providersResponse))
            .thenReturn(Mono.just(signInUrlResponse));
            
        when(mockResponseSpec.bodyToMono(AuthResponse.class))
            .thenReturn(Mono.just(authResponse));

        return mockWebClient;
    }
    
    @Bean
    @Primary
    public WebClient supabaseAdminClient() {
        // For simplicity, we can reuse the same mock setup
        return supabaseClient();
    }
} 