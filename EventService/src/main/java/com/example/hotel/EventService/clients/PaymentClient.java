package com.example.hotel.EventService.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "user-auth-service", url = "http://localhost:8081")
public interface PaymentClient {
    @PostMapping("/api/payments/process")
    ResponseEntity<Map<String, Object>> processPayment(
            @RequestParam("userId") Long userId,
            @RequestParam("amount") Double amount);

    @GetMapping("/api/payments/balance/{userId}")
    ResponseEntity<Map<String, Object>> getBalance(@PathVariable("userId") Long userId);
}