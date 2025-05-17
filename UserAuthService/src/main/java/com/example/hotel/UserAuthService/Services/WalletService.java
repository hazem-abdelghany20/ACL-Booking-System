package com.example.hotel.UserAuthService.Services;

import com.example.hotel.UserAuthService.models.User;
import com.example.hotel.UserAuthService.models.Wallet;
import com.example.hotel.UserAuthService.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Placeholder payment service that uses in-memory storage instead of a database.
 * This will be replaced with proper Supabase integration in the future.
 * Service for handling wallet operations through Supabase
 */
@Service
public class WalletService {
    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final WebClient supabaseClient;

    public WalletService(@Qualifier("supabaseClient") WebClient supabaseClient) {
        this.supabaseClient = supabaseClient;
    }

    public Mono<Wallet> getWallet(String userId) {
        logger.info("Getting wallet for user: {}", userId);

        return supabaseClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/wallets")
                        .queryParam("user_id", "eq." + userId)
                        .queryParam("select", "*")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Wallet>>() {})
                .flatMap(wallets -> {
                    if (wallets == null || wallets.isEmpty()) {
                        logger.info("No wallet found for user: {}. Creating one...", userId);
                        return createWallet(userId);
                    } else {
                        logger.info("Wallet found for user: {}", userId);
                        return Mono.just(wallets.get(0));
                    }
                })
                .onErrorResume(error -> {
                    logger.error("Error getting wallet: {}", error.getMessage());
                    return createWallet(userId);
                });
    }

    /**
     * Creates a wallet for a user with initial balance
     * @param userId User ID
     * @return Created wallet
     */
    private Mono<Wallet> createWallet(String userId) {
        logger.info("Creating wallet for user: {}", userId);

        Map<String, Object> walletData = new HashMap<>();
        walletData.put("user_id", userId);
        walletData.put("balance", 1000.00); // Initial balance

        return supabaseClient
                .post()
                .uri("/rest/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(walletData)
                .retrieve()
                .bodyToMono(Wallet.class)
                .doOnSuccess(wallet -> logger.info("Wallet created successfully for user: {}", userId))
                .onErrorResume(error -> {
                    logger.error("Error creating wallet: {}", error.getMessage());
                    // Return a default wallet in case of error
                    Wallet defaultWallet = new Wallet();
                    defaultWallet.setUserId(userId);
                    defaultWallet.setBalance(1000.00);
                    return Mono.just(defaultWallet);
                });
    }



//    @Transactional
//    public boolean processPayment(Long userId, Double amount) {
//        Double balance = userBalances.getOrDefault(userId, 0.0);

    /**
     * Gets or creates a wallet for a user
     * @param userId User ID
     * @return Wallet object
     */


//    @Transactional
//    public Double depositToWallet(Long userId, Double amount) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
//
//        user.setBalance(user.getBalance() + amount);
//        userRepository.save(user);
//        return user.getBalance();
//    }



//
//    public Double getBalance(Long userId) {
//        return userBalances.getOrDefault(userId, 0.0);
//    }
    /**
     * Add funds to a wallet
     * @param userId User ID
     * @param amount Amount to add
     * @return Updated wallet
     */
    public Mono<Wallet> addFunds(String userId, Double amount) {
        logger.info("Adding {} funds to wallet for user: {}", amount, userId);

        return getWallet(userId)
                .flatMap(wallet -> {
                    Double newBalance = wallet.getBalance() + amount;

                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("balance", newBalance);

                    return supabaseClient
                            .patch()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/rest/v1/wallets")
                                    .queryParam("user_id", "eq." + userId)
                                    .build())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .bodyValue(updateData)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .then(Mono.defer(() -> {
                                wallet.setBalance(newBalance);
                                return Mono.just(wallet);
                            }));
                });
    }

    /**
     * Deduct funds from a wallet
     * @param userId User ID
     * @param amount Amount to deduct
     * @return Updated wallet
     */
    public Mono<Wallet> deductFunds(String userId, Double amount) {
        logger.info("Deducting {} funds from wallet for user: {}", amount, userId);

        return getWallet(userId)
                .flatMap(wallet -> {
                    if (wallet.getBalance() < amount) {
                        return Mono.error(new IllegalArgumentException("Insufficient funds"));
                    }

                    Double newBalance = wallet.getBalance() - amount;

                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("balance", newBalance);

                    return supabaseClient
                            .patch()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/rest/v1/wallets")
                                    .queryParam("user_id", "eq." + userId)
                                    .build())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .bodyValue(updateData)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .then(Mono.defer(() -> {
                                wallet.setBalance(newBalance);
                                return Mono.just(wallet);
                            }));
                });
    }
}