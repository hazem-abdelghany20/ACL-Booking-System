package com.example.hotel.NotificationService.command;

import com.example.hotel.NotificationService.command.MarkAsReadCommand;
import com.example.hotel.NotificationService.command.MarkAsUnreadCommand;
import com.example.hotel.NotificationService.repositories.NotificationRepository;

public class CommandHandler {

    private final NotificationRepository repository;

    public CommandHandler(NotificationRepository repository) {
        this.repository = repository;
    }

    public void execute(String action, String id) throws Exception {
        switch (action) {
            case "read" -> new MarkAsReadCommand(repository).execute(id);
            case "unread" -> new MarkAsUnreadCommand(repository).execute(id);
            default -> throw new IllegalArgumentException("Invalid action: " + action);
        }
    }
}
