package com.example.hotel.NotificationService.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document("notifications")
public class Notification {

    @Id
    private String id;

    private Long userId;
    private String type; // e.g., "Signup", "Reminder"
    private String title;
    private String message;
    private String status; // "read" or "unread"
    private LocalDateTime createdAt;
    private Map<String, String> metadata;

    public Notification() {}

    public Notification(Long userId, String type, String title, String message, String status, LocalDateTime createdAt, Map<String, String> metadata) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.metadata = metadata;
    }

    // Getters and Setters

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Map<String, String> getMetadata() { return metadata; }

    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
}
