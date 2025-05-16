package com.example.hotel.NotificationService.dto;

import java.util.Map;

public class NotificationRequest {
    private Long userId;
    private String type; // e.g., "Signup", "Reminder"
    private String title;
    private String message;
    private Map<String, String> metadata;

    public NotificationRequest() {}

    public NotificationRequest(Long userId, String type, String title, String message, Map<String, String> metadata) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.metadata = metadata;
    }

    // Getters and Setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
