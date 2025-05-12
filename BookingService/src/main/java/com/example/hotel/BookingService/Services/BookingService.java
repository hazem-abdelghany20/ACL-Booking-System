package com.example.hotel.BookingService.Services;

import com.example.hotel.BookingService.Clients.EventClient;
import com.example.hotel.BookingService.rabbitmq.BookingProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class BookingService {
    private final EventClient eventClient;
    private final BookingProducer producer;

    @Value("${Name}")
    String name;

    @Value("${ID}")
    String id;

    public BookingService(EventClient eventClient,
                         BookingProducer producer) {
        this.eventClient = eventClient;
        this.producer = producer;
    }
    
    public String createEventBooking(Long eventId, Long userId) {
        // Check ticket availability via EventClient
        Map<String, Object> availabilityResponse = eventClient.getAvailableTickets(eventId);
        int availableTickets = (int) availabilityResponse.get("availableTickets");
        
        if (availableTickets <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No tickets available for this event");
        }
        
        // Generate a random booking ID using UUID
        String bookingId = UUID.randomUUID().toString();
        
        // Send booking notification via RabbitMQ
        producer.sendBooking(bookingId + " " + name + "_" + id);
        
        return bookingId;
    }
}

