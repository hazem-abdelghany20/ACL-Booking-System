package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.MockWalletService;
import com.example.hotel.UserAuthService.payload.response.WalletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for wallet operations using the mock wallet service
 */
@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    private final MockWalletService mockWalletService;

    public WalletController(MockWalletService mockWalletService) {
        this.mockWalletService = mockWalletService;
    }

    // Helper method to extract token
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null; // Return null instead of potentially invalid token
    }

//    @GetMapping("/balance")
//    public Mono<ResponseEntity<WalletResponse>> getBalance(
//            @RequestParam String userId,
//            @RequestHeader("Authorization") String authHeader) {
//        logger.info("Getting wallet balance for user: {}", userId);
//
//        // Extract the token properly
//        String token = extractToken(authHeader);
//
//        if (token == null) {
//            logger.error("Invalid or missing token in Authorization header");
//            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new WalletResponse("Invalid or missing token", null, 0.0)));
//        }
//
//        // Log token info for debugging
//        logger.info("WALLET CONTROLLER: Token with length: {}, Token first/last 10 chars: {} / {}",
//                token.length(),
//                token.substring(0, Math.min(10, token.length())),
//                token.substring(Math.max(0, token.length() - 10)));
//
//        return mockWalletService.getWalletBalance(userId, token)
//                .map(walletResponse -> ResponseEntity.ok().body(walletResponse))
//                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
//    }

    @GetMapping("/balance-alt")
    public Mono<ResponseEntity<Map<String, Object>>> getWalletBalance(
            @RequestParam String userId,
            @RequestHeader("Authorization") String authHeader) {
        // Simply delegate to the main getBalance method
        return getBalance(userId, authHeader);
    }

    @GetMapping("/balance")
    public Mono<ResponseEntity<Map<String, Object>>> getBalance(
            @RequestParam String userId,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Getting wallet balance for user: {}", userId);

        // Extract the token properly
        String token = extractToken(authHeader);

        if (token == null) {
            logger.error("Invalid or missing token in Authorization header");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid or missing token");
            errorResponse.put("userId", userId);
            errorResponse.put("balance", 0.0);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        }

        // Log token info for debugging
        logger.info("WALLET CONTROLLER: Token with length: {}, Token first/last 10 chars: {} / {}",
                token.length(),
                token.substring(0, Math.min(10, token.length())),
                token.substring(Math.max(0, token.length() - 10)));

        return mockWalletService.getWalletBalance(userId, token)
                .map(walletResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("userId", walletResponse.getUserId());
                    response.put("balance", walletResponse.getBalance());
                    //response.put("success", walletResponse.isSuccess());
                    if (walletResponse.getMessage() != null) {
                        response.put("message", walletResponse.getMessage());
                    }
                    return ResponseEntity.ok().body(response);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PostMapping("/add-funds")
    public Mono<ResponseEntity<Map<String, Object>>> addFunds(
            @RequestParam String userId,
            @RequestParam Double amount,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Adding funds to wallet: {} for user: {}", amount, userId);

        // Extract the token properly
        String token = extractToken(authHeader);

        if (token == null) {
            logger.error("Invalid or missing token in Authorization header");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid or missing token");
            errorResponse.put("userId", userId);
            errorResponse.put("balance", 0.0);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        }

        return mockWalletService.addFunds(userId, amount, token)
                .map(walletResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("userId", walletResponse.getUserId());
                    response.put("balance", walletResponse.getBalance());
                    //response.put("success", walletResponse.isSuccess());
                    if (walletResponse.getMessage() != null) {
                        response.put("message", walletResponse.getMessage());
                    }
                    return ResponseEntity.ok().body(response);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PostMapping("/deduct-funds")
    public Mono<ResponseEntity<Map<String, Object>>> deductFunds(
            @RequestParam String userId,
            @RequestParam Double amount,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Deducting funds from wallet: {} for user: {}", amount, userId);

        // Extract the token properly
        String token = extractToken(authHeader);

        if (token == null) {
            logger.error("Invalid or missing token in Authorization header");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid or missing token");
            errorResponse.put("userId", userId);
            errorResponse.put("balance", 0.0);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        }

        return mockWalletService.deductFunds(userId, amount, token)
                .map(walletResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("userId", walletResponse.getUserId());
                    response.put("balance", walletResponse.getBalance());
                    //response.put("success", walletResponse.isSuccess());
                    if (walletResponse.getMessage() != null) {
                        response.put("message", walletResponse.getMessage());
                    }
                    return ResponseEntity.ok().body(response);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PostMapping("/create-wallet")
    public Mono<ResponseEntity<Map<String, Object>>> createWallet(
            @RequestParam String userId,
            @RequestParam(required = false) Double initialBalance,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Creating wallet for user: {}", userId);

        // Extract the token properly
        String token = extractToken(authHeader);

        if (token == null) {
            logger.error("Invalid or missing token in Authorization header");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid or missing token");
            errorResponse.put("userId", userId);
            errorResponse.put("balance", 0.0);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        }

        // Use default initial balance if not provided
        double balance = initialBalance != null ? initialBalance : 1000.0;

        return mockWalletService.createWalletExplicitly(userId, balance, token)
                .map(walletResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("userId", walletResponse.getUserId());
                    response.put("balance", walletResponse.getBalance());
                    //response.put("success", walletResponse.isSuccess());
                    if (walletResponse.getMessage() != null) {
                        response.put("message", walletResponse.getMessage());
                    }
                    return ResponseEntity.ok().body(response);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/transactions")
    public Mono<ResponseEntity<Map<String, Object>>> getTransactionHistory(
            @RequestParam String userId,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Getting transaction history for user: {}", userId);

        // Extract the token properly
        String token = extractToken(authHeader);

        if (token == null) {
            logger.error("Invalid or missing token in Authorization header");
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Invalid or missing token");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
        }

        return mockWalletService.getTransactionHistory(userId, token)
                .map(transactions -> ResponseEntity.ok().body(transactions))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

//    @PostMapping("/create-wallet")
//    public Mono<ResponseEntity<WalletResponse>> createWallet(
//            @RequestParam String userId,
//            @RequestParam(required = false) Double initialBalance,
//            @RequestHeader("Authorization") String authHeader) {
//        logger.info("Creating wallet for user: {}", userId);
//
//        // Extract the token properly
//        String token = extractToken(authHeader);
//
//        if (token == null) {
//            logger.error("Invalid or missing token in Authorization header");
//            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new WalletResponse("Invalid or missing token", null, 0.0)));
//        }
//
//        // Use default initial balance if not provided
//        double balance = initialBalance != null ? initialBalance : 1000.0;
//
//        return mockWalletService.createWalletExplicitly(userId, balance, token)
//                .map(walletResponse -> ResponseEntity.ok().body(walletResponse))
//                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
//    }

    @GetMapping("/check-wallet")
    public Mono<ResponseEntity<Map<String, Object>>> checkWalletExists(
            @RequestParam String userId,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Checking if wallet exists for user: {}", userId);

        // Extract the token properly
        String token = extractToken(authHeader);

        if (token == null) {
            logger.error("Invalid or missing token in Authorization header");
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Invalid or missing token");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
        }

        return mockWalletService.checkWalletExists(userId, token)
                .map(result -> ResponseEntity.ok().body(result));
    }

    @GetMapping("/test-token")
    public ResponseEntity<Map<String, String>> testToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Received request to /test-token");

        Map<String, String> response = new HashMap<>();

        if (authHeader == null) {
            logger.warn("No Authorization header received");
            response.put("error", "No Authorization header provided");
            return ResponseEntity.badRequest().body(response);
        }

        logger.info("Received Authorization header: {}", authHeader);

        String token = extractToken(authHeader);
        logger.info("Extracted token: {}", token);

        response.put("originalHeader", authHeader);
        response.put("extractedToken", token);
        response.put("supabaseTest", "Success (mocked)");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/init-db")
    public ResponseEntity<Map<String, Object>> initializeDatabase() {
        logger.info("Mock initializing wallet database");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Mock wallet database initialized successfully");

        return ResponseEntity.ok(response);
    }
}