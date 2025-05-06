package com.example.hotel.NotificationService.command;

import com.example.hotel.NotificationService.command.NotificationCommand;
import com.example.hotel.NotificationService.repositories.NotificationRepository;

public class MarkAsReadCommand implements NotificationCommand {

    private final NotificationRepository repository;

    public MarkAsReadCommand(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(String notificationId) {
        repository.findById(notificationId).ifPresent(notification -> {
            notification.setStatus("read");
            repository.save(notification);
        });
    }
}
