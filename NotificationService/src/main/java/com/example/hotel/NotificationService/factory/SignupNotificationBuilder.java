package com.example.hotel.NotificationService.factory;

import com.example.hotel.NotificationService.models.Notification;

import java.time.LocalDateTime;
import java.util.HashMap;

public class SignupNotificationBuilder implements NotificationBuilder {

    @Override
    public Notification build(Long userId, String title, String message) {
        return new Notification(
                userId,
                "Signup",
                title,
                message,
                "unread",
                LocalDateTime.now(),
                new HashMap<>()
        );
    }
}
