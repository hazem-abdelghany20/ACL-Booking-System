package com.example.hotel.UserAuthService.auth.factory;

import com.example.hotel.UserAuthService.auth.strategy.AuthStrategy;
import com.example.hotel.UserAuthService.auth.strategy.EmailAuthStrategy;
import com.example.hotel.UserAuthService.auth.strategy.GoogleAuthStrategy;
import org.springframework.stereotype.Component;

/**
 * Factory for creating authentication strategies
 */
@Component
public class AuthStrategyFactory {
    
    private final EmailAuthStrategy emailAuthStrategy;
    private final GoogleAuthStrategy googleAuthStrategy;
    
    public AuthStrategyFactory(
            EmailAuthStrategy emailAuthStrategy,
            GoogleAuthStrategy googleAuthStrategy) {
        this.emailAuthStrategy = emailAuthStrategy;
        this.googleAuthStrategy = googleAuthStrategy;
    }
    
    /**
     * Create an authentication strategy based on the authentication type
     * @param authType Authentication type
     * @return The appropriate authentication strategy
     */
    public AuthStrategy createStrategy(AuthType authType) {
        switch (authType) {
            case EMAIL:
                return emailAuthStrategy;
            case GOOGLE:
                return googleAuthStrategy;
            default:
                throw new IllegalArgumentException("Unsupported authentication type: " + authType);
        }
    }
    
    /**
     * Enum for authentication types
     */
    public enum AuthType {
        EMAIL,
        GOOGLE
    }
} 