package com.example.hotel.BookingService.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "user-auth-service", url = "http://localhost:8081")
public interface UserClient {

//    @PostMapping("api/wallet/process")
//    ResponseEntity<Map<String,Object>> processPayment
//            (@RequestParam("userId") Long userId,
//             @RequestParam("amount") Double amount);
//
//    @GetMapping("api/wallet/balance/{userId}")
//    ResponseEntity<Map<String,Object>> getBalance (@PathVariable("userId") Long userId);

    @GetMapping("/api/wallet/balance")
    ResponseEntity<Map<String, Object>> getWalletBalance(
            @RequestParam("userId") String userId,
            @RequestHeader("Authorization") String token);

    @PostMapping("/api/wallet/add-funds")
    ResponseEntity<Map<String, Object>> addFunds(
            @RequestParam("userId") String userId,
            @RequestParam("amount") Double amount,
            @RequestHeader("Authorization") String token);

    @PostMapping("/api/wallet/deduct-funds")
    ResponseEntity<Map<String, Object>> deductFunds(
            @RequestParam("userId") String userId,
            @RequestParam("amount") Double amount,
            @RequestHeader("Authorization") String token);

    @GetMapping("/api/wallet/transactions")
    ResponseEntity<Map<String, Object>> getTransactionHistory(
            @RequestParam("userId") String userId,
            @RequestHeader("Authorization") String token);

    @PostMapping("/api/users/{userId}/events/{eventId}")
    ResponseEntity<?> addEventToUser(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId);

    @DeleteMapping("/api/users/{userId}/events/{eventId}")
    ResponseEntity<?> removeEventFromUser(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId);
}
