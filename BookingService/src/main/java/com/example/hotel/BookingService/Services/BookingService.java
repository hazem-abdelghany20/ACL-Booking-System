package com.example.hotel.BookingService.Services;

import com.example.hotel.BookingService.Clients.EventClient;
import com.example.hotel.BookingService.Entities.Booking;
import com.example.hotel.BookingService.Entities.BookingStatus;
import com.example.hotel.BookingService.Clients.AvailabilityClient;
import com.example.hotel.BookingService.Clients.UserClient;
import com.example.hotel.BookingService.rabbitmq.BookingProducer;
import com.example.hotel.BookingService.Repositories.BookingRepository;
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

    private final AvailabilityClient availabilityClient;
    private final EventClient eventClient;
    private final BookingProducer producer;
    private final BookingRepository bookingRepository;
    private final UserClient userClient;

    @Value("${Name}") private String name;
    @Value("${ID}")   private String id;
    // Default auth token to use with wallet API
    private static final String DEFAULT_AUTH_TOKEN = "Bearer default-token";

    public BookingService(AvailabilityClient availabilityClient,
                          EventClient        eventClient,
                          UserClient         userClient,
                          BookingRepository  bookingRepository,
                          BookingProducer    producer) {
        this.availabilityClient = availabilityClient;
        this.eventClient        = eventClient;
        this.userClient         = userClient;
        this.bookingRepository  = bookingRepository;
        this.producer           = producer;
    }

    // Create a new booking: check availability, persist, decrement tickets, notify.
    public String createEventBooking(Long eventId, Long userId) {
        // Check ticket availability
        @SuppressWarnings("unchecked")
        Map<String, Object> avail = eventClient.getAvailableTickets(eventId);
        int available = (int) avail.get("availableTickets");
        if (available <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No tickets available for event " + eventId);
        }

        // Persist booking
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, eventId, userId, BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Decrement tickets on the event
        eventClient.adjustAvailableTickets(eventId, -1);

        // Send notification
        producer.sendBooking(bookingId + " " + name + "_" + id);
        return bookingId;
    }

    // Cancel an existing booking: only CONFIRMED → CANCELLED, free a ticket, notify.
    public void cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found: " + bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only CONFIRMED bookings can be cancelled");
        }

        // free up one ticket
        eventClient.adjustAvailableTickets(booking.getEventId(), +1);

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        producer.sendBooking(bookingId + " CANCELLED");
    }

    //Refund a booking: only CANCELLED → REFUNDED, no ticket adjustment, notify.
    public void refundBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found: " + bookingId));

        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only CANCELLED bookings can be refunded");
        }

        booking.setStatus(BookingStatus.REFUNDED);
        bookingRepository.save(booking);

        producer.sendBooking(bookingId + " REFUNDED");
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

            // Convert Long userId to String for wallet API
            String userIdStr = String.valueOf(userId);

            // Process the payment using the updated wallet API
            ResponseEntity<Map<String, Object>> response = userClient.deductFunds(
                    userIdStr,
                    price,
                    DEFAULT_AUTH_TOKEN
            );

            Map<String, Object> responseBody = response.getBody();
            boolean paymentSuccess = (responseBody != null && response.getStatusCode() == HttpStatus.OK);

            if (!paymentSuccess) {
                return "Payment failed: Insufficient funds";
            }

            try {
                eventClient.addParticipantToEvent(eventId, userId);
                userClient.addEventToUser(userId, eventId);

                System.out.println("Payment processed successfully");

                // Get updated balance
                ResponseEntity<Map<String, Object>> balanceResponse = userClient.getWalletBalance(
                        userIdStr,
                        DEFAULT_AUTH_TOKEN
                );

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

            // Convert Long userId to String for wallet API
            String userIdStr = String.valueOf(userId);

            // Process the refund using the updated wallet API
            ResponseEntity<Map<String, Object>> response = userClient.addFunds(
                    userIdStr,
                    price,
                    DEFAULT_AUTH_TOKEN
            );

            Map<String, Object> responseBody = response.getBody();
            boolean refundSuccess = (responseBody != null && response.getStatusCode() == HttpStatus.OK);

            if (!refundSuccess) {
                return "Refund failed";
            }

            try {
                eventClient.removeParticipantFromEvent(eventId, userId);
                userClient.removeEventFromUser(userId, eventId);

                // Get updated balance
                ResponseEntity<Map<String, Object>> balanceResponse = userClient.getWalletBalance(
                        userIdStr,
                        DEFAULT_AUTH_TOKEN
                );

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