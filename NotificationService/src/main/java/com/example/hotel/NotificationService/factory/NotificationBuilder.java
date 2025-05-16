package com.example.hotel.NotificationService.factory;

import com.example.hotel.NotificationService.models.Notification;

public interface NotificationBuilder {
    Notification build(Long userId, String title, String message);
}
