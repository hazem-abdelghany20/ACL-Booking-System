package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.WalletService;
import com.example.hotel.UserAuthService.auth.service.AuthService;
import com.example.hotel.UserAuthService.models.Wallet;
import com.example.hotel.UserAuthService.payload.request.WalletTransactionRequest;
import com.example.hotel.UserAuthService.payload.response.MessageResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller for wallet operations
 */
@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WalletController {
    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);
    
    private final WalletService walletService;
    private final AuthService authService;
    
    public WalletController(WalletService walletService, AuthService authService) {
        this.walletService = walletService;
        this.authService = authService;
    }
    
    /**
     * Get a user's wallet balance
     */
    @GetMapping("/balance")
    public Mono<ResponseEntity<Wallet>> getWalletBalance(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userId = authService.getUserIdFromToken(token);
        
        if (userId == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        return walletService.getWallet(userId, token)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    
    /**
     * Add funds to a user's wallet
     */
    @PostMapping("/add-funds")
    public Mono<ResponseEntity<Wallet>> addFunds(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody WalletTransactionRequest request) {
        
        String token = authHeader.replace("Bearer ", "");
        String userId = authService.getUserIdFromToken(token);
        
        if (userId == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        return walletService.addFunds(userId, request.getAmount(), token)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
    
    /**
     * Deduct funds from a user's wallet
     */
    @PostMapping("/deduct-funds")
    public Mono<ResponseEntity<Wallet>> deductFunds(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody WalletTransactionRequest request) {
        
        String token = authHeader.replace("Bearer ", "");
        String userId = authService.getUserIdFromToken(token);
        
        if (userId == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        return walletService.deductFunds(userId, request.getAmount(), token)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    logger.error("Error deducting funds: {}", error.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
                });
    }
} 