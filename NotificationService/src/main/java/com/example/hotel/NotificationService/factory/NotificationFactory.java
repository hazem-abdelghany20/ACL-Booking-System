package com.example.hotel.NotificationService.factory;

public class NotificationFactory {

    public static NotificationBuilder getBuilder(String type) {
        return switch (type) {
            case "Signup" -> new SignupNotificationBuilder();
            case "Reminder" -> new ReminderNotificationBuilder();
            default -> throw new IllegalArgumentException("Unknown notification type: " + type);
        };
    }
}
