package com.example.hotel.BookingService.Controllers;

import com.example.hotel.BookingService.Services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService service;
    
    @Value("${Name}")
    String name;

    @Value("${ID}")
    String id;

    @Autowired
    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> book(@RequestParam String roomType, @RequestParam int nights) {
        String bookingId = service.createBooking(roomType, nights);
        return ResponseEntity.ok(bookingId + " " + name + "_" + id);
    }

    @PostMapping("/events/payment")
    public ResponseEntity<Map<String, Object>> processEventPayment(@RequestParam Long userId, @RequestParam Long eventId) {
        try {
            String result = service.processEventPayment(userId, eventId);

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
            String result = service.processEventRefund(userId, eventId);

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

