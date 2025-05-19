package com.example.hotel.UserAuthService.Services;

import com.example.hotel.UserAuthService.payload.response.WalletResponse;
import com.example.hotel.UserAuthService.models.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock wallet service implementation that doesn't rely on Supabase tables
 * This is a temporary solution for project submission
 */
@Service
public class MockWalletService {
    private static final Logger logger = LoggerFactory.getLogger(MockWalletService.class);

    // In-memory storage of wallets
    private final ConcurrentHashMap<String, Wallet> wallets = new ConcurrentHashMap<>();

    // In-memory storage of transactions
    private final List<Map<String, Object>> transactions = new ArrayList<>();

    /**
     * Get wallet balance
     */
    public Mono<WalletResponse> getWalletBalance(String userId, String token) {
        logger.info("Getting wallet balance for user: {}", userId);

        Wallet wallet = wallets.get(userId);

        if (wallet == null) {
            logger.info("Wallet not found for user: {}. Creating a new wallet.", userId);
            return createWallet(userId, 1000.0);
        } else {
            logger.info("Wallet found for user: {} with balance: {}", userId, wallet.getBalance());
            return Mono.just(new WalletResponse(wallet));
        }
    }

    /**
     * Create wallet
     */
    public Mono<WalletResponse> createWallet(String userId, Double initialBalance) {
        logger.info("Creating wallet for user: {} with initial balance: {}", userId, initialBalance);

        Wallet newWallet = new Wallet();
        newWallet.setId(UUID.randomUUID().toString());
        newWallet.setUserId(userId);
        newWallet.setBalance(initialBalance != null ? initialBalance : 1000.0);
        newWallet.setCreatedAt(Instant.now());
        newWallet.setUpdatedAt(Instant.now());

        wallets.put(userId, newWallet);

        // Record transaction
        recordTransaction(userId, initialBalance, "credit", "Initial deposit");

        logger.info("Wallet created successfully with balance: {}", newWallet.getBalance());
        return Mono.just(new WalletResponse(newWallet));
    }

    /**
     * Add funds to wallet
     */
    public Mono<WalletResponse> addFunds(String userId, Double amount, String token) {
        logger.info("Adding {} funds to wallet for user: {}", amount, userId);

        // Get wallet
        Wallet wallet = wallets.get(userId);

        if (wallet == null) {
            logger.info("Wallet not found for user: {}. Creating a new wallet.", userId);
            return createWallet(userId, amount);
        }

        // Update balance
        double currentBalance = wallet.getBalance() != null ? wallet.getBalance() : 0.0;
        double newBalance = currentBalance + amount;
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(Instant.now());

        // Record transaction
        recordTransaction(userId, amount, "credit", "Funds added");

        logger.info("Successfully updated wallet balance to: {}", newBalance);
        return Mono.just(new WalletResponse(wallet));
    }

    /**
     * Deduct funds from wallet
     */
    public Mono<WalletResponse> deductFunds(String userId, Double amount, String token) {
        logger.info("Deducting {} funds from wallet for user: {}", amount, userId);

        // Get wallet
        Wallet wallet = wallets.get(userId);

        if (wallet == null) {
            logger.info("Wallet not found for user: {}.", userId);
            return Mono.just(new WalletResponse("Wallet not found", userId, 0.0));
        }

        // Check if sufficient funds
        double currentBalance = wallet.getBalance() != null ? wallet.getBalance() : 0.0;
        if (currentBalance < amount) {
            logger.info("Insufficient funds for user: {}", userId);
            return Mono.just(new WalletResponse("Insufficient funds", userId, currentBalance));
        }

        // Update balance
        double newBalance = currentBalance - amount;
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(Instant.now());

        // Record transaction
        recordTransaction(userId, amount, "debit", "Funds deducted");

        logger.info("Successfully updated wallet balance to: {}", newBalance);
        return Mono.just(new WalletResponse(wallet));
    }

    /**
     * Check if wallet exists
     */
    public Mono<Map<String, Object>> checkWalletExists(String userId, String token) {
        logger.info("Checking if wallet exists for user: {}", userId);

        boolean exists = wallets.containsKey(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("userId", userId);

        if (exists) {
            Wallet wallet = wallets.get(userId);
            Map<String, Object> walletData = new HashMap<>();
            walletData.put("id", wallet.getId());
            walletData.put("user_id", wallet.getUserId());
            walletData.put("balance", wallet.getBalance());
            walletData.put("created_at", wallet.getCreatedAt());
            walletData.put("updated_at", wallet.getUpdatedAt());
            response.put("data", walletData);
        }

        return Mono.just(response);
    }

    /**
     * Get transaction history
     */
    public Mono<Map<String, Object>> getTransactionHistory(String userId, String token) {
        logger.info("Getting transaction history for user: {}", userId);

        List<Map<String, Object>> userTransactions = new ArrayList<>();
        for (Map<String, Object> tx : transactions) {
            if (tx.get("user_id").equals(userId)) {
                userTransactions.add(tx);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("transactions", userTransactions);
        response.put("success", true);

        return Mono.just(response);
    }

    /**
     * Record transaction
     */
    private void recordTransaction(String userId, Double amount, String type, String description) {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("id", UUID.randomUUID().toString());
        transaction.put("user_id", userId);
        transaction.put("amount", amount);
        transaction.put("type", type);
        transaction.put("description", description);
        transaction.put("created_at", Instant.now());

        transactions.add(transaction);
    }

    /**
     * Create wallet explicitly
     */
    public Mono<WalletResponse> createWalletExplicitly(String userId, Double initialBalance, String token) {
        logger.info("Explicitly creating wallet for user: {} with initial balance: {}", userId, initialBalance);

        // Check if wallet already exists
        if (wallets.containsKey(userId)) {
            Wallet existingWallet = wallets.get(userId);
            logger.info("Wallet already exists for user: {}", userId);
            return Mono.just(new WalletResponse(existingWallet.getUserId(), existingWallet.getBalance(), false, "Wallet already exists for this user"));
        }

        // Create new wallet
        return createWallet(userId, initialBalance);
    }
}