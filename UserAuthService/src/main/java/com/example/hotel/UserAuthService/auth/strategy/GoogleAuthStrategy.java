package com.example.hotel.UserAuthService.auth.strategy;

import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of Google OAuth authentication strategy
 */
@Component
public class GoogleAuthStrategy implements AuthStrategy {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthStrategy.class);
    
    private final WebClient supabaseClient;
    
    @Value("${app.oauth.redirect-url:http://localhost:8081/api/oauth/callback}")
    private String redirectUrl;
    
    @Value("${supabase.url}")
    private String supabaseUrl;
    
    public GoogleAuthStrategy(WebClient supabaseClient) {
        this.supabaseClient = supabaseClient;
    }
    
    @Override
    public Mono<AuthResponse> authenticate(Object credentials) {
        // For Google OAuth, the credentials would be the authorization code from the OAuth callback
        if (!(credentials instanceof String)) {
            return Mono.error(new IllegalArgumentException("Invalid credentials type for Google authentication"));
        }
        
        String code = (String) credentials;
        logger.info("Exchanging OAuth code for session");
        
        return supabaseClient.post()
                .uri("/auth/v1/token?grant_type=authorization_code")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("code", code))
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .doOnSuccess(response -> logger.info("Successfully exchanged code for session"))
                .doOnError(error -> logger.error("Error exchanging code for session: {}", error.getMessage()));
    }
    
    @Override
    public Mono<AuthResponse> register(Object credentials) {
        // Google OAuth doesn't have a separate registration process, so we'll just return an error
        return Mono.error(new UnsupportedOperationException("Direct registration is not supported for Google authentication"));
    }
    
    /**
     * Get the OAuth URL for Google sign-in
     * @return Map containing the URL to redirect the user to for Google sign-in
     */
    public Mono<Map> getGoogleSignInUrl() {
        logger.info("Building Google sign-in URL with redirect: {}", redirectUrl);
        
        // Build a map with the URL to return
        Map<String, String> result = new HashMap<>();
        
        // Construct the Supabase OAuth URL manually
        String state = UUID.randomUUID().toString();
        String googleAuthUrl = supabaseUrl + "/auth/v1/authorize?provider=google" +
                "&redirect_to=" + redirectUrl +
                "&state=" + state;
        
        result.put("url", googleAuthUrl);
        logger.info("Created Google sign-in URL: {}", googleAuthUrl);
        
        return Mono.just(result);
    }
} 