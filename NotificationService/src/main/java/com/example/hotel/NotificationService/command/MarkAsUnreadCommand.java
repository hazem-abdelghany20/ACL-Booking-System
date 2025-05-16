package com.example.hotel.NotificationService.command;

import com.example.hotel.NotificationService.models.Notification;
import com.example.hotel.NotificationService.repositories.NotificationRepository;

public class MarkAsUnreadCommand implements NotificationCommand {

    private final NotificationRepository repository;

    public MarkAsUnreadCommand(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(String notificationId) {
        repository.findById(notificationId).ifPresent(notification -> {
            notification.setStatus("unread");
            repository.save(notification);
        });
    }
}
