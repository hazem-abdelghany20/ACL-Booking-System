package com.example.hotel.BookingService.Controllers;

import com.example.hotel.BookingService.Services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
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

}

