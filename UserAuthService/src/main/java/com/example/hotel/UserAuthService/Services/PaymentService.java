package com.example.hotel.UserAuthService.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

/**
 * Placeholder payment service that uses in-memory storage instead of a database.
 * This will be replaced with proper Supabase integration in the future.
 */
@Service
public class PaymentService {

    // Simple in-memory user balance map for testing
    private static final Map<Long, Double> userBalances = new HashMap<>();

    // Initialize with some test data
    static {
        userBalances.put(1L, 1000.0);
        userBalances.put(2L, 500.0);
    }

    @Transactional
    public boolean processPayment(Long userId, Double amount) {
        Double balance = userBalances.getOrDefault(userId, 0.0);
        
        if (balance >= amount) {
            userBalances.put(userId, balance - amount);
            return true;
        }
        return false;
    }

    @Transactional
    public Double addFunds(Long userId, Double amount) {
        Double balance = userBalances.getOrDefault(userId, 0.0);
        Double newBalance = balance + amount;
        userBalances.put(userId, newBalance);
        return newBalance;
    }

    public Double getBalance(Long userId) {
        return userBalances.getOrDefault(userId, 0.0);
    }
}