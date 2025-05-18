package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import com.example.hotel.UserAuthService.payload.response.WalletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
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
    private final WebClient supabaseClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    public WalletController(SupabaseAuthService supabaseAuthService, WebClient supabaseClient) {
        this.supabaseAuthService = supabaseAuthService;
        this.supabaseClient = supabaseClient;
    }

    // Helper method to extract token
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    /**
     * Get the balance of the user's wallet
     * @param userId User ID
     * @param authHeader Authorization token
     * @return Wallet balance
     */
    @GetMapping("/balance")
    public Mono<ResponseEntity<WalletResponse>> getBalance(
            @RequestParam String userId,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Getting wallet balance for user: {}", userId);

        // Extract the token properly
        String token = extractToken(authHeader);

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
            @RequestHeader("Authorization") String authHeader) {
        // Extract the token properly
        String token = extractToken(authHeader);
        return getBalance(userId, token);
    }

    /**
     * Add funds to the user's wallet
     * @param userId User ID
     * @param amount Amount to add
     * @param authHeader Authorization token
     * @return Updated wallet
     */
    @PostMapping("/add-funds")
    public Mono<ResponseEntity<WalletResponse>> addFunds(
            @RequestParam String userId,
            @RequestParam Double amount,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Adding funds to wallet: {} for user: {}", amount, userId);

        // Extract the token properly
        String token = extractToken(authHeader);

        return supabaseAuthService.addFunds(userId, amount, token)
                .map(walletResponse -> ResponseEntity.ok().body(walletResponse))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Deduct funds from the user's wallet
     * @param userId User ID
     * @param amount Amount to deduct
     * @param authHeader Authorization token
     * @return Updated wallet
     */
    @PostMapping("/deduct-funds")
    public Mono<ResponseEntity<WalletResponse>> deductFunds(
            @RequestParam String userId,
            @RequestParam Double amount,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Deducting funds from wallet: {} for user: {}", amount, userId);

        // Extract the token properly
        String token = extractToken(authHeader);

        return supabaseAuthService.deductFunds(userId, amount, token)
                .map(walletResponse -> ResponseEntity.ok().body(walletResponse))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Get transaction history for a user
     * @param userId User ID
     * @param authHeader Authorization token
     * @return Transaction history
     */
    @GetMapping("/transactions")
    public Mono<ResponseEntity<Map>> getTransactionHistory(
            @RequestParam String userId,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Getting transaction history for user: {}", userId);

        // Extract the token properly
        String token = extractToken(authHeader);

        return supabaseAuthService.getTransactionHistory(userId, token)
                .map(transactions -> ResponseEntity.ok().body(transactions))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
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

        // Try a simple Supabase call with the token
        try {
            logger.info("Testing token with Supabase");

            supabaseClient.get()
                    .uri("/auth/v1/user")
                    .headers(headers -> headers.set("Authorization", "Bearer " + token))
                    .retrieve()
                    .toBodilessEntity()
                    .block(); // Use block for simplicity in this test endpoint

            response.put("supabaseTest", "Success");
            logger.info("Supabase test successful");
        } catch (Exception e) {
            logger.error("Error testing with Supabase: {}", e.getMessage(), e);
            response.put("supabaseTest", "Failed: " + e.getMessage());

            if (e instanceof WebClientResponseException) {
                WebClientResponseException wcre = (WebClientResponseException) e;
                response.put("statusCode", wcre.getStatusCode().toString());
                response.put("responseBody", wcre.getResponseBodyAsString());
                logger.error("Supabase response: {} - {}", wcre.getStatusCode(), wcre.getResponseBodyAsString());
            }
        }

        return ResponseEntity.ok(response);
    }


    @GetMapping("/check-token")
    public ResponseEntity<Map<String, Object>> checkToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        String token = extractToken(authHeader);
        response.put("tokenLength", token.length());
        response.put("tokenStart", token.substring(0, Math.min(10, token.length())));
        response.put("tokenEnd", token.substring(Math.max(0, token.length() - 10)));

        // Check if token has the expected JWT structure (header.payload.signature)
        String[] parts = token.split("\\.");
        response.put("partsCount", parts.length);

        if (parts.length == 3) {
            response.put("isValidJwtFormat", true);
        } else {
            response.put("isValidJwtFormat", false);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/direct-test")
    public ResponseEntity<Map<String, Object>> directTest(@RequestHeader("Authorization") String authHeader) {
        System.out.println("================== DIRECT TEST STARTED ==================");
        System.out.println("AUTH HEADER: " + authHeader);

        Map<String, Object> response = new HashMap<>();
        String token = extractToken(authHeader);

        System.out.println("EXTRACTED TOKEN: " + token);
        System.out.println("TOKEN LENGTH: " + token.length());

        response.put("token", token);

        logger.info("Running direct test with token: {}", token.substring(0, Math.min(10, token.length())) + "...");

        // Get the token parts for debugging
        String[] parts = token.split("\\.");
        if (parts.length == 3) {
            logger.info("Token has valid JWT format with 3 parts");
        } else {
            logger.warn("Token does NOT have valid JWT format. Parts: {}", parts.length);
        }

        try {
            // Create a completely new WebClient instance
            WebClient newClient = WebClient.builder()
                    .baseUrl(supabaseUrl)
                    .build();

            logger.info("Attempting direct call to Supabase with custom headers");

            String result = newClient.get()
                    .uri("/rest/v1/wallets?limit=1")
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            response.put("status", "success");
            response.put("result", result);
            logger.info("Direct call successful!");
        } catch (Exception e) {
            logger.error("Direct call failed: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", e.getMessage());

            if (e instanceof WebClientResponseException) {
                WebClientResponseException wcre = (WebClientResponseException) e;
                response.put("statusCode", wcre.getStatusCode().toString());
                response.put("responseBody", wcre.getResponseBodyAsString());
                logger.error("Supabase response: {} - {}", wcre.getStatusCode(), wcre.getResponseBodyAsString());
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-anon")
    public ResponseEntity<Map<String, Object>> testAnonAccess() {
        Map<String, Object> response = new HashMap<>();

        logger.info("Testing anonymous access with Supabase anon key");

        try {
            WebClient newClient = WebClient.builder()
                    .baseUrl(supabaseUrl)
                    .build();

            String result = newClient.get()
                    .uri("/rest/v1/wallets?limit=1")
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey) // Use anon key as Bearer token
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            response.put("status", "success");
            response.put("result", result);
            logger.info("Anonymous access successful!");
        } catch (Exception e) {
            logger.error("Anonymous access failed: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", e.getMessage());

            if (e instanceof WebClientResponseException) {
                WebClientResponseException wcre = (WebClientResponseException) e;
                response.put("statusCode", wcre.getStatusCode().toString());
                response.put("responseBody", wcre.getResponseBodyAsString());
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/raw-token")
    public String rawToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null) {
            return "No Authorization header provided";
        }

        String token = extractToken(authHeader);
        return "Token: " + token;
    }


}