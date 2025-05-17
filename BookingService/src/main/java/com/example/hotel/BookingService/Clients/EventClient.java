package com.example.hotel.BookingService.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "event-service", url = "http://event-service:8081")
public interface EventClient {
    @GetMapping("/api/events/{id}")
    ResponseEntity<Map<String, Object>> getEvent(@PathVariable("id") Long id);


    @PostMapping("/api/events/{eventId}/participants/{userId}")
    ResponseEntity<?> addParticipantToEvent(
            @PathVariable("eventId") Long eventId,
            @PathVariable("userId") Long userId);


    @DeleteMapping("/api/events/{eventId}/participants/{userId}")
    ResponseEntity<?> removeParticipantFromEvent(
            @PathVariable("eventId") Long eventId,
            @PathVariable("userId") Long userId);

    @GetMapping("/api/events/{eventId}/participants/{userId}")
    ResponseEntity<?> isUserRegisteredForEvent(
            @PathVariable("eventId") Long eventId,
            @PathVariable("userId") Long userId);


    @GetMapping("/api/events/{eventId}/available-tickets")
    Map<String, Object> getAvailableTickets(@PathVariable Long eventId);

    @PutMapping("/api/events/{eventId}/available-tickets")
    void adjustAvailableTickets(
            @PathVariable Long eventId,
            @RequestParam("delta") int delta
    );
}
