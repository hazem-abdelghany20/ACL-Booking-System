package com.example.hotel.BookingService.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "user-auth-service", url = "http://localhost:8081")


public interface UserClient {

    @PostMapping("/api/payments/process")
    ResponseEntity<Map<String, Object>> processPayment(
            @RequestParam("userId") Long userId,
            @RequestParam("amount") Double amount);

    @GetMapping("/api/payments/balance/{userId}")
    ResponseEntity<Map<String, Object>> getBalance(@PathVariable("userId") Long userId);


    @PostMapping("/api/users/{userId}/events/{eventId}")
    ResponseEntity<?> addEventToUser(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId);


    @DeleteMapping("/api/users/{userId}/events/{eventId}")
    ResponseEntity<?> removeEventFromUser(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId);
}