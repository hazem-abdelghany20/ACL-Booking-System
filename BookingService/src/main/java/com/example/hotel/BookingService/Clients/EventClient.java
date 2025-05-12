package com.example.hotel.BookingService.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "event-service", url = "http://localhost:8081")
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
}