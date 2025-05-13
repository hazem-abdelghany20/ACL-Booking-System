package com.example.hotel.BookingService.Services;

import com.example.hotel.BookingService.Clients.EventClient;
import com.example.hotel.BookingService.Clients.AvailabilityClient;
import com.example.hotel.BookingService.Clients.UserClient;
import com.example.hotel.BookingService.rabbitmq.BookingProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class BookingService {

    private final AvailabilityClient availability;
    private final EventClient eventClient;
    private final BookingProducer producer;
    private final UserClient userClient;



    @Value("${Name}")
    String name;

    @Value("${ID}")
    String id;


    public BookingService(AvailabilityClient availability,
                          BookingProducer producer,
                          UserClient userClient,
                          EventClient eventClient) {
        this.availability = availability;
        this.producer = producer;
        this.userClient = userClient;
        this.eventClient = eventClient;
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


    @Transactional
    public String processEventPayment(Long userId, Long eventId) {
        try {

            ResponseEntity<Map<String, Object>> eventResponse = eventClient.getEvent(eventId);

            if (eventResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to retrieve event with ID: " + eventId);
            }

            Map<String, Object> eventData = eventResponse.getBody();
            if (eventData == null) {
                throw new RuntimeException("Event data is null for event ID: " + eventId);
            }


            try {

                ResponseEntity<?> participantCheckResponse = eventClient.isUserRegisteredForEvent(eventId, userId);


                Map<String, Object> checkResponseBody = (Map<String, Object>) participantCheckResponse.getBody();
                boolean isAlreadyRegistered = false;

                if (checkResponseBody != null && checkResponseBody.containsKey("isRegistered")) {
                    isAlreadyRegistered = (boolean) checkResponseBody.get("isRegistered");
                }

                if (isAlreadyRegistered) {
                    return "You are already registered for this event: " + eventData.get("title");
                }
            } catch (Exception e) {

                System.err.println("Warning: Could not verify registration status: " + e.getMessage());
            }


            Double price = (Double) eventData.get("price");


            if (price < 0) {
                throw new IllegalStateException("Invalid event price: " + price);
            }


            if (price == 0) {
                try {

                    eventClient.addParticipantToEvent(eventId, userId);

                    userClient.addEventToUser(userId, eventId);

                    return "Successfully registered for free event: " + eventData.get("title");
                } catch (Exception e) {
                    throw new RuntimeException("Error registering for free event: " + e.getMessage());
                }
            }


            ResponseEntity<Map<String, Object>> response = userClient.processPayment(userId, price);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !(Boolean)responseBody.get("success")) {
                return "Payment failed: Insufficient funds";
            }


            try {

                eventClient.addParticipantToEvent(eventId, userId);

                userClient.addEventToUser(userId, eventId);


                System.out.println("Payment response: " + responseBody);


                ResponseEntity<Map<String, Object>> balanceResponse = userClient.getBalance(userId);
                Double remainingBalance = 0.0;

                if (balanceResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> balanceBody = balanceResponse.getBody();
                    if (balanceBody != null && balanceBody.containsKey("balance")) {
                        remainingBalance = (Double) balanceBody.get("balance");
                    }
                }

                return "Payment processed successfully for event: " + eventData.get("title") +
                        ". You are now registered for this event. Your new balance is: " + remainingBalance;
            } catch (Exception e) {


                return "Payment processed but there was an issue registering for the event: " + e.getMessage() +
                        ". Please contact support.";
            }

        } catch (Exception e) {
            throw new IllegalStateException("Error processing payment: " + e.getMessage());
        }
    }

    public String processEventRefund(Long userId, Long eventId) {
        try {

            ResponseEntity<Map<String, Object>> eventResponse = eventClient.getEvent(eventId);

            if (eventResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to retrieve event with ID: " + eventId);
            }

            Map<String, Object> eventData = eventResponse.getBody();
            if (eventData == null) {
                throw new RuntimeException("Event data is null for event ID: " + eventId);
            }


            try {

                ResponseEntity<?> participantCheckResponse = eventClient.isUserRegisteredForEvent(eventId, userId);


                Map<String, Object> checkResponseBody = (Map<String, Object>) participantCheckResponse.getBody();
                boolean isRegistered = false;

                if (checkResponseBody != null && checkResponseBody.containsKey("isRegistered")) {
                    isRegistered = (boolean) checkResponseBody.get("isRegistered");
                }

                if (!isRegistered) {
                    return "Refund failed: You are not registered for this event.";
                }
            } catch (Exception e) {

                return "Unable to verify registration status: " + e.getMessage();
            }


            Double price = (Double) eventData.get("price");


            if (price == 0) {
                try {

                    eventClient.removeParticipantFromEvent(eventId, userId);


                    userClient.removeEventFromUser(userId, eventId);

                    return "Successfully unregistered from free event: " + eventData.get("title");
                } catch (Exception e) {
                    throw new RuntimeException("Error unregistering from free event: " + e.getMessage());
                }
            }


            if (price < 0) {
                return "No refund because refund amount is negative";
            }


            ResponseEntity<Map<String, Object>> response = userClient.processPayment(userId, -price);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !(Boolean)responseBody.get("success")) {
                return "Refund failed";
            }

            try {

                eventClient.removeParticipantFromEvent(eventId, userId);


                userClient.removeEventFromUser(userId, eventId);


                ResponseEntity<Map<String, Object>> balanceResponse = userClient.getBalance(userId);
                Double newBalance = 0.0;

                if (balanceResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> balanceBody = balanceResponse.getBody();
                    if (balanceBody != null && balanceBody.containsKey("balance")) {
                        newBalance = (Double) balanceBody.get("balance");
                    }
                }


                return "Refund of " + price + " " + eventData.get("currency") +
                        " processed successfully for event: " + eventData.get("title") +
                        ". You are now unregistered from this event. Your new balance is: " + newBalance;
            } catch (Exception e) {


                return "Refund processed but there was an issue unregistering from the event: " + e.getMessage() +
                        ". Please contact support.";
            }

        } catch (Exception e) {
            throw new IllegalStateException("Error processing refund: " + e.getMessage());
        }
    }


}

