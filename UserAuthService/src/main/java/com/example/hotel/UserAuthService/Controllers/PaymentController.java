package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(
            @RequestParam Long userId,
            @RequestParam Double amount) {

        boolean success = paymentService.processPayment(userId, amount);
        Map<String, Object> response = new HashMap<>();

        response.put("success", success);
        if (success) {
            response.put("message", "Payment processed successfully");
            response.put("remainingBalance", paymentService.getBalance(userId));
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Insufficient balance");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/add-funds")
    public ResponseEntity<Map<String, Object>> addFunds(
            @RequestParam Long userId,
            @RequestParam Double amount) {

        Double newBalance = paymentService.addFunds(userId, amount);
        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("message", "Funds added successfully");
        response.put("newBalance", newBalance);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long userId) {
        Double balance = paymentService.getBalance(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("balance", balance);

        return ResponseEntity.ok(response);
    }
}