package com.example.hotel.UserAuthService.auth.session;

import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton session manager for user authentication sessions
 */
@Component
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    // Singleton instance (lazy initialization) with double-checked locking pattern
    private static volatile SessionManager instance;
    
    // Use thread-safe map for storing user sessions
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();
    
    // Private constructor to prevent instantiation
    private SessionManager() {
        logger.info("Initializing SessionManager singleton");
    }
    
    /**
     * Get the singleton instance of SessionManager
     * @return The SessionManager instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Create a new session for a user
     * @param userId User ID
     * @param authResponse Authentication response containing tokens
     * @return The created user session
     */
    public UserSession createSession(String userId, AuthResponse authResponse) {
        logger.info("Creating session for user: {}", userId);
        
        UserSession session = new UserSession(
                userId,
                authResponse.getAccessToken(),
                authResponse.getRefreshToken(),
                authResponse.getExpiresIn()
        );
        
        sessions.put(userId, session);
        return session;
    }
    
    /**
     * Get a user session by user ID
     * @param userId User ID
     * @return The user session if it exists, null otherwise
     */
    public UserSession getSession(String userId) {
        return sessions.get(userId);
    }
    
    /**
     * Remove a user session
     * @param userId User ID
     */
    public void removeSession(String userId) {
        logger.info("Removing session for user: {}", userId);
        sessions.remove(userId);
    }
    
    /**
     * Check if a session is valid and not expired
     * @param userId User ID
     * @return True if the session is valid, false otherwise
     */
    public boolean isSessionValid(String userId) {
        UserSession session = sessions.get(userId);
        if (session == null) {
            return false;
        }
        
        return Instant.now().isBefore(session.getExpiresAt());
    }
    
    /**
     * Get the total number of active sessions
     * @return Number of active sessions
     */
    public int getActiveSessionCount() {
        return (int) sessions.values().stream()
                .filter(session -> Instant.now().isBefore(session.getExpiresAt()))
                .count();
    }
    
    /**
     * Clear all sessions
     */
    public void clearAllSessions() {
        logger.info("Clearing all sessions");
        sessions.clear();
    }
    
    /**
     * Get all sessions
     * @return Map of all sessions
     */
    public Map<String, UserSession> getAllSessions() {
        return new ConcurrentHashMap<>(sessions); // Return a copy to prevent external modification
    }
    
    /**
     * Inner class representing a user session
     */
    public static class UserSession {
        private final String userId;
        private String accessToken;
        private String refreshToken;
        private final Instant createdAt;
        private Instant expiresAt;
        
        public UserSession(String userId, String accessToken, String refreshToken, long expiresInSeconds) {
            this.userId = userId;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.createdAt = Instant.now();
            this.expiresAt = createdAt.plusSeconds(expiresInSeconds);
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
        
        public Instant getCreatedAt() {
            return createdAt;
        }
        
        public Instant getExpiresAt() {
            return expiresAt;
        }
        
        public void setExpiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
        }
        
        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
        
        public void updateExpiry(long expiresInSeconds) {
            this.expiresAt = Instant.now().plusSeconds(expiresInSeconds);
        }
    }
} 