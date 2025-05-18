package com.example.hotel.UserAuthService.auth.service;

import com.example.hotel.UserAuthService.auth.factory.AuthStrategyFactory;
import com.example.hotel.UserAuthService.auth.factory.AuthStrategyFactory.AuthType;
import com.example.hotel.UserAuthService.auth.session.SessionManager;
import com.example.hotel.UserAuthService.auth.strategy.GoogleAuthStrategy;
import com.example.hotel.UserAuthService.payload.request.EmailAuthRequest;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling authentication using the strategy factory and session manager
 */
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final AuthStrategyFactory authStrategyFactory;
    private final GoogleAuthStrategy googleAuthStrategy;
    private final SessionManager sessionManager;
    private final WebClient supabaseClient;
    
    public AuthService(
            AuthStrategyFactory authStrategyFactory,
            GoogleAuthStrategy googleAuthStrategy,
            WebClient supabaseClient) {
        this.authStrategyFactory = authStrategyFactory;
        this.googleAuthStrategy = googleAuthStrategy;
        this.sessionManager = SessionManager.getInstance();
        this.supabaseClient = supabaseClient;
    }
    
    /**
     * Authenticate a user with email and password
     * @param request Email auth request
     * @return Authentication response
     */
    public Mono<AuthResponse> signInWithEmail(EmailAuthRequest request) {
        logger.info("Authenticating user with email: {}", request.getEmail());
        
        return authStrategyFactory.createStrategy(AuthType.EMAIL)
                .authenticate(request)
                .doOnSuccess(response -> {
                    if (response != null && response.getUser() != null) {
                        sessionManager.createSession(response.getUserId(), response);
                    }
                });
    }
    
    /**
     * Register a user with email and password
     * @param request Email auth request
     * @return Authentication response
     */
    public Mono<AuthResponse> signUpWithEmail(EmailAuthRequest request) {
        logger.info("Registering user with email: {}", request.getEmail());
        
        return authStrategyFactory.createStrategy(AuthType.EMAIL)
                .register(request)
                .doOnSuccess(response -> {
                    if (response != null && response.getUser() != null) {
                        sessionManager.createSession(response.getUserId(), response);
                    }
                });
    }
    
    /**
     * Get Google OAuth URL for sign-in
     * @return Map containing the redirect URL
     */
    public Mono<Map> getGoogleSignInUrl() {
        return googleAuthStrategy.getGoogleSignInUrl();
    }
    
    /**
     * Authenticate with Google OAuth code
     * @param code OAuth code
     * @return Authentication response
     */
    public Mono<AuthResponse> authenticateWithGoogle(String code) {
        logger.info("Authenticating with Google OAuth code");
        
        return authStrategyFactory.createStrategy(AuthType.GOOGLE)
                .authenticate(code)
                .doOnSuccess(response -> {
                    if (response != null && response.getUser() != null) {
                        sessionManager.createSession(response.getUserId(), response);
                    }
                });
    }
    
    /**
     * Sign out a user
     * @param userId User ID
     */
    public void signOut(String userId) {
        logger.info("Signing out user: {}", userId);
        sessionManager.removeSession(userId);
    }
    
    /**
     * Check if a session is valid
     * @param userId User ID
     * @return True if the session is valid
     */
    public boolean validateSession(String userId) {
        return sessionManager.isSessionValid(userId);
    }
    
    /**
     * Get session token for a user
     * @param userId User ID
     * @return Access token if the session exists, null otherwise
     */
    public String getSessionToken(String userId) {
        SessionManager.UserSession session = sessionManager.getSession(userId);
        return session != null ? session.getAccessToken() : null;
    }
    
    /**
     * Get user ID from access token by checking the sessions
     * @param token Access token
     * @return User ID if found, null otherwise
     */
    public String getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        // Loop through all sessions to find one with this token
        Map<String, SessionManager.UserSession> sessions = sessionManager.getAllSessions();
        for (Map.Entry<String, SessionManager.UserSession> entry : sessions.entrySet()) {
            if (token.equals(entry.getValue().getAccessToken())) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    /**
     * Reset a user's password by sending a reset email
     * @param email User email
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> resetPassword(String email) {
        logger.info("Requesting password reset for email: {}", email);
        
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        
        return supabaseClient.post()
                .uri("/auth/v1/recover")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.toBodilessEntity().then(Mono.<Void>empty());
                    } else {
                        return response.createException().flatMap(Mono::error);
                    }
                })
                .doOnSuccess(aVoid -> logger.info("Successfully initiated password reset for email: {}", email))
                .doOnError(error -> logger.error("Error sending password reset email to {}: {}", email, error.getMessage()));
    }
} 