package com.example.hotel.NotificationService.rabbitmq;

import com.example.hotel.NotificationService.dto.NotificationRequest;
import com.example.hotel.NotificationService.factory.NotificationFactory;
import com.example.hotel.NotificationService.models.Notification;
import com.example.hotel.NotificationService.repositories.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON parser
    private final NotificationRepository repository;

    public NotificationListener(NotificationRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMQConfig.BOOKING_QUEUE)
    public void handleIncomingMessage(String messageJson) {
        try {
            NotificationRequest request = objectMapper.readValue(messageJson, NotificationRequest.class);

            Notification notification = NotificationFactory
                    .getBuilder(request.getType())
                    .build(request.getUserId(), request.getTitle(), request.getMessage());

            notification.setMetadata(request.getMetadata());
            repository.save(notification);

            System.out.println("Saved notification for user " + request.getUserId());

        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
        }
    }
}
