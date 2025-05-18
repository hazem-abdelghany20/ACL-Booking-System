package com.example.hotel.UserAuthService.auth.strategy;

import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import reactor.core.publisher.Mono;

/**
 * Strategy interface for different authentication methods (Strategy Pattern)
 */
public interface AuthStrategy {
    /**
     * Authenticate a user
     * @param credentials The authentication credentials
     * @return Authentication response
     */
    Mono<AuthResponse> authenticate(Object credentials);
    
    /**
     * Register a new user
     * @param credentials The registration credentials
     * @return Authentication response
     */
    Mono<AuthResponse> register(Object credentials);
} 