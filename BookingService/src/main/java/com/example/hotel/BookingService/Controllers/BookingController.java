package com.example.hotel.BookingService.Controllers;

import com.example.hotel.BookingService.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.hotel.BookingService.Clients.AvailabilityClient;
import com.example.hotel.BookingService.rabbitmq.BookingProducer;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private AvailabilityClient availabilityClient;
    @Autowired
    private BookingProducer bookingProducer;

    @Value("${Name}") private String name;
    @Value("${ID}")   private String id;

    @PostMapping(params = {"roomType","nights"})
    public ResponseEntity<String> bookRoom(
            @RequestParam("roomType") String roomType,
            @RequestParam("nights")   int nights) {
                boolean ok = availabilityClient.check(roomType, nights);
               if (!ok) {
                        return ResponseEntity.badRequest().build();
               }
                String bookingId = UUID.randomUUID().toString();
                bookingProducer.sendBooking(bookingId);
                return ResponseEntity.ok(bookingId);
            }
    @PostMapping("/events/{eventId}")
    public ResponseEntity<Map<String, Object>> createEventBooking(
            @PathVariable Long eventId,
            @RequestParam Long userId) {
        try {
            String bookingId = bookingService.createEventBooking(eventId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("bookingId", bookingId);
            response.put("eventId", eventId);
            response.put("userId", userId);
            response.put("status", "CONFIRMED");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancel(@PathVariable String bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookingId}/refund")
    public ResponseEntity<Void> refund(@PathVariable String bookingId) {
        bookingService.refundBooking(bookingId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/events/payment")
    public ResponseEntity<Map<String, Object>> processEventPayment(@RequestParam Long userId, @RequestParam Long eventId) {
        try {
            String result = bookingService.processEventPayment(userId, eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error processing payment: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Process refund for an event
     */
    @PostMapping("/events/refund")
    public ResponseEntity<Map<String, Object>> processEventRefund(@RequestParam Long userId, @RequestParam Long eventId) {
        try {
            String result = bookingService.processEventRefund(userId, eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error processing refund: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

