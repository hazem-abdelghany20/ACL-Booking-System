package com.example.hotel.UserAuthService.Services;

import com.example.hotel.UserAuthService.models.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service for handling wallet operations through Supabase
 */
@Service
public class WalletService {
    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);
    
    private final WebClient supabaseClient;
    
    public WalletService(@Qualifier("supabaseClient") WebClient supabaseClient) {
        this.supabaseClient = supabaseClient;
    }
    
    /**
     * Get a user's wallet
     * @param userId User ID
     * @param token Authentication token
     * @return Wallet if found
     */
    public Mono<Wallet> getWallet(String userId, String token) {
        logger.info("Getting wallet for user: {}", userId);
        
        return supabaseClient.get()
                .uri("/rest/v1/wallets?user_id=eq." + userId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Wallet>>() {})
                .map(wallets -> {
                    if (wallets != null && !wallets.isEmpty()) {
                        logger.info("Found wallet for user: {}", userId);
                        return wallets.get(0);
                    } else {
                        logger.info("No wallet found for user: {}, creating a new one", userId);
                        return createWallet(userId, token).block();
                    }
                })
                .doOnError(error -> logger.error("Error getting wallet: {}", error.getMessage()));
    }
    
    /**
     * Create a new wallet for a user with initial balance of 0
     * @param userId User ID
     * @param token Authentication token
     * @return Created wallet
     */
    private Mono<Wallet> createWallet(String userId, String token) {
        logger.info("Creating wallet for user: {}", userId);
        
        Wallet newWallet = new Wallet();
        newWallet.setUserId(userId);
        newWallet.setBalance(0.0);
        
        return supabaseClient.post()
                .uri("/rest/v1/wallets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newWallet)
                .retrieve()
                .bodyToMono(Wallet.class)
                .doOnSuccess(wallet -> logger.info("Created wallet for user: {}", userId))
                .doOnError(error -> logger.error("Error creating wallet: {}", error.getMessage()));
    }
    
    /**
     * Add funds to a user's wallet
     * @param userId User ID
     * @param amount Amount to add
     * @param token Authentication token
     * @return Updated wallet
     */
    public Mono<Wallet> addFunds(String userId, Double amount, String token) {
        logger.info("Adding {} funds to wallet for user: {}", amount, userId);
        
        return supabaseClient.post()
                .uri("/rest/v1/rpc/add_funds")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("user_id", userId, "amount", amount))
                .retrieve()
                .bodyToMono(Wallet.class)
                .doOnSuccess(wallet -> logger.info("Added funds to wallet for user: {}", userId))
                .doOnError(error -> logger.error("Error adding funds to wallet: {}", error.getMessage()));
    }
    
    /**
     * Deduct funds from a user's wallet
     * @param userId User ID
     * @param amount Amount to deduct
     * @param token Authentication token
     * @return Updated wallet
     */
    public Mono<Wallet> deductFunds(String userId, Double amount, String token) {
        logger.info("Deducting {} funds from wallet for user: {}", amount, userId);
        
        return supabaseClient.post()
                .uri("/rest/v1/rpc/deduct_funds")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("user_id", userId, "amount", amount))
                .retrieve()
                .bodyToMono(Wallet.class)
                .doOnSuccess(wallet -> logger.info("Deducted funds from wallet for user: {}", userId))
                .doOnError(error -> logger.error("Error deducting funds from wallet: {}", error.getMessage()));
    }
} 