package com.example.hotel.NotificationService;

import com.example.hotel.NotificationService.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @RabbitListener(queues = RabbitMQConfig.BOOKING_QUEUE)
    public void onNewBooking(String bookingId) {
        System.out.println("Received new booking from 52-8936: " + bookingId);
    }
}
