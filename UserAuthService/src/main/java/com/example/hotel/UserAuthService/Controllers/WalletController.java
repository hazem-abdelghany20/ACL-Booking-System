package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import com.example.hotel.UserAuthService.payload.response.WalletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controller for wallet operations
 */
@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    private final SupabaseAuthService supabaseAuthService;

    public WalletController(SupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }

    /**
     * Get the balance of the user's wallet
     * @param userId User ID
     * @param token Authorization token
     * @return Wallet balance
     */
    @GetMapping("/balance")
    public Mono<ResponseEntity<WalletResponse>> getBalance(
            @RequestParam String userId,
            @RequestHeader("Authorization") String token) {
        logger.info("Getting wallet balance for user: {}", userId);

        return supabaseAuthService.getWalletBalance(userId, token)
                .map(walletResponse -> ResponseEntity.ok().body(walletResponse))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Get the balance of the user's wallet (alternative method)
     * Same functionality but to match test naming
     */
    @GetMapping("/balance-alt")
    public Mono<ResponseEntity<WalletResponse>> getWalletBalance(
            @RequestParam String userId,
            @RequestHeader("Authorization") String token) {
        return getBalance(userId, token);
    }

    /**
     * Add funds to the user's wallet
     * @param userId User ID
     * @param amount Amount to add
     * @param token Authorization token
     * @return Updated wallet
     */
    @PostMapping("/add-funds")
    public Mono<ResponseEntity<WalletResponse>> addFunds(
            @RequestParam String userId,
            @RequestParam Double amount,
            @RequestHeader("Authorization") String token) {
        logger.info("Adding funds to wallet: {} for user: {}", amount, userId);

        return supabaseAuthService.addFunds(userId, amount, token)
                .map(walletResponse -> ResponseEntity.ok().body(walletResponse))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Deduct funds from the user's wallet
     * @param userId User ID
     * @param amount Amount to deduct
     * @param token Authorization token
     * @return Updated wallet
     */
    @PostMapping("/deduct-funds")
    public Mono<ResponseEntity<WalletResponse>> deductFunds(
            @RequestParam String userId,
            @RequestParam Double amount,
            @RequestHeader("Authorization") String token) {
        logger.info("Deducting funds from wallet: {} for user: {}", amount, userId);

        return supabaseAuthService.deductFunds(userId, amount, token)
                .map(walletResponse -> ResponseEntity.ok().body(walletResponse))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Get transaction history for a user
     * @param userId User ID
     * @param token Authorization token
     * @return Transaction history
     */
    @GetMapping("/transactions")
    public Mono<ResponseEntity<Map>> getTransactionHistory(
            @RequestParam String userId,
            @RequestHeader("Authorization") String token) {
        logger.info("Getting transaction history for user: {}", userId);

        return supabaseAuthService.getTransactionHistory(userId, token)
                .map(transactions -> ResponseEntity.ok().body(transactions))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}