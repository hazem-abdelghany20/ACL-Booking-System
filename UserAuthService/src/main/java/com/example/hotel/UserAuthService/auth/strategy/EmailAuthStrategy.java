package com.example.hotel.UserAuthService.auth.strategy;

import com.example.hotel.UserAuthService.payload.request.EmailAuthRequest;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of email authentication strategy
 */
@Component
public class EmailAuthStrategy implements AuthStrategy {
    private static final Logger logger = LoggerFactory.getLogger(EmailAuthStrategy.class);
    
    private final WebClient supabaseClient;
    
    public EmailAuthStrategy(WebClient supabaseClient) {
        this.supabaseClient = supabaseClient;
    }
    
    @Override
    public Mono<AuthResponse> authenticate(Object credentials) {
        if (!(credentials instanceof EmailAuthRequest)) {
            return Mono.error(new IllegalArgumentException("Invalid credentials type for email authentication"));
        }
        
        EmailAuthRequest request = (EmailAuthRequest) credentials;
        logger.info("Signing in user with email: {}", request.getEmail());
        
        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getPassword());
        
        return supabaseClient.post()
                .uri("/auth/v1/token?grant_type=password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .doOnSuccess(response -> logger.info("Successfully signed in user with email: {}", request.getEmail()))
                .doOnError(error -> logger.error("Error signing in user with email: {}", error.getMessage()));
    }
    
    @Override
    public Mono<AuthResponse> register(Object credentials) {
        if (!(credentials instanceof EmailAuthRequest)) {
            return Mono.error(new IllegalArgumentException("Invalid credentials type for email registration"));
        }
        
        EmailAuthRequest request = (EmailAuthRequest) credentials;
        logger.info("Signing up user with email: {}", request.getEmail());
        
        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getPassword());
        
        return supabaseClient.post()
                .uri("/auth/v1/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .doOnSuccess(response -> logger.info("Successfully signed up user with email: {}", request.getEmail()))
                .doOnError(error -> logger.error("Error signing up user with email: {}", error.getMessage()));
    }
} 