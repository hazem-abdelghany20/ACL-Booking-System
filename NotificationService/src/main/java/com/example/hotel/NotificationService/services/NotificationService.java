package com.example.hotel.NotificationService.services;

import com.example.hotel.NotificationService.command.CommandHandler;
import com.example.hotel.NotificationService.dto.NotificationRequest;
import com.example.hotel.NotificationService.factory.NotificationFactory;
import com.example.hotel.NotificationService.models.Notification;
import com.example.hotel.NotificationService.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final CommandHandler commandHandler;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
        this.commandHandler = new CommandHandler(repository); // manually inject here
    }

    public Notification createNotification(NotificationRequest request) {
        Notification notification = NotificationFactory
                .getBuilder(request.getType())
                .build(request.getUserId(), request.getTitle(), request.getMessage());

        notification.setMetadata(request.getMetadata());
        return repository.save(notification);
    }

    public List<Notification> getAllForUser(Long userId) {
        return repository.findByUserId(userId);
    }

    public void executeCommand(String action, String notificationId) throws Exception {
        commandHandler.execute(action, notificationId);
    }
}
