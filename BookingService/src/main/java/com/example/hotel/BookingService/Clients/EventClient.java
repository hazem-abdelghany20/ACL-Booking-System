package com.example.hotel.BookingService.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", url = "http://event-service:8081")
public interface EventClient {
    
    @GetMapping("/api/events/{eventId}/available-tickets")
    Map<String, Object> getAvailableTickets(@PathVariable Long eventId);
} 