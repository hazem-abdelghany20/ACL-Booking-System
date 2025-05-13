package com.example.hotel.BookingService.Controllers;

import com.example.hotel.BookingService.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    @Autowired
    private BookingService bookingService;

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

