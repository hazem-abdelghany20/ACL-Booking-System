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
     * Get the balance of the user's wallet
     * @param token Authorization token
     * @return Wallet balance
     */
    @GetMapping("/balance")
    public Mono<ResponseEntity<Object>> getBalance(@RequestHeader("Authorization") String token) {
        logger.info("Getting wallet balance");
        
        // Extract token value from "Bearer <token>"
        String tokenValue = token.substring(7);
        String userId = authService.getUserIdFromToken(tokenValue);
        
        if (userId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not found in session")));
        }
        
        return walletService.getWallet(userId)
                .map(wallet -> ResponseEntity.ok().body((Object)wallet))
                .onErrorResume(error -> Mono.just(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body((Object)new MessageResponse("Error getting wallet: " + error.getMessage()))));
    }
    
    /**
     * Add funds to the user's wallet
     * @param request Wallet transaction request
     * @param token Authorization token
     * @return Updated wallet
     */
    @PostMapping("/add-funds")
    public Mono<ResponseEntity<Object>> addFunds(
            @Valid @RequestBody WalletTransactionRequest request,
            @RequestHeader("Authorization") String token) {
        logger.info("Adding funds to wallet: {}", request.getAmount());
        
        // Extract token value from "Bearer <token>"
        String tokenValue = token.substring(7);
        String userId = authService.getUserIdFromToken(tokenValue);
        
        if (userId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not found in session")));
        }
        
        return walletService.addFunds(userId, request.getAmount())
                .map(wallet -> ResponseEntity.ok().body((Object)wallet))
                .onErrorResume(error -> Mono.just(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body((Object)new MessageResponse("Error adding funds: " + error.getMessage()))));
    }
    
    /**
     * Deduct funds from the user's wallet
     * @param request Wallet transaction request
     * @param token Authorization token
     * @return Updated wallet
     */
    @PostMapping("/deduct-funds")
    public Mono<ResponseEntity<Object>> deductFunds(
            @Valid @RequestBody WalletTransactionRequest request,
            @RequestHeader("Authorization") String token) {
        logger.info("Deducting funds from wallet: {}", request.getAmount());
        
        // Extract token value from "Bearer <token>"
        String tokenValue = token.substring(7);
        String userId = authService.getUserIdFromToken(tokenValue);
        
        if (userId == null) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not found in session")));
        }
        
        return walletService.deductFunds(userId, request.getAmount())
                .map(wallet -> ResponseEntity.ok().body((Object)wallet))
                .onErrorResume(error -> {
                    if (error instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body((Object)new MessageResponse(error.getMessage())));
                    }
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body((Object)new MessageResponse("Error deducting funds: " + error.getMessage())));
                });
    }
} 