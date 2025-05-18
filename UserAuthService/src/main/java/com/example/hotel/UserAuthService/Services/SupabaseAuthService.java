package com.example.hotel.UserAuthService.Services;

import com.example.hotel.UserAuthService.payload.request.EmailAuthRequest;
import com.example.hotel.UserAuthService.payload.request.OtpVerificationRequest;
import com.example.hotel.UserAuthService.payload.request.PhoneAuthRequest;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import com.example.hotel.UserAuthService.payload.response.WalletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import com.example.hotel.UserAuthService.models.Wallet;
import org.springframework.core.ParameterizedTypeReference;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated This service is being replaced by the new auth strategy pattern implementation.
 * Use {@link com.example.hotel.UserAuthService.auth.service.AuthService} instead.
 */
@Service
@Deprecated
public class SupabaseAuthService {
    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthService.class);
    
    private final WebClient supabaseClient;
    private final WebClient supabaseAdminClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    
    public SupabaseAuthService(
            @Qualifier("supabaseClient") WebClient supabaseClient,
            @Qualifier("supabaseAdminClient") WebClient supabaseAdminClient) {
        this.supabaseClient = supabaseClient;
        this.supabaseAdminClient = supabaseAdminClient;
    }
    
    /**
     * Sign up with email and password
     */
    public Mono<AuthResponse> signUpWithEmail(EmailAuthRequest request) {
        logger.info("Signing up user with email: {}", request.getEmail());
        
        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getPassword());
        
        return supabaseClient.post()
                .uri("/auth/v1/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .doOnSuccess(response -> logger.info("Successfully signed up user with email: {}", request.getEmail()))
                .doOnError(error -> logger.error("Error signing up user with email: {}", error.getMessage()));
    }
    
    /**
     * Sign in with email and password
     */
    public Mono<AuthResponse> signInWithEmail(EmailAuthRequest request) {
        logger.info("Signing in user with email: {}", request.getEmail());
        
        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getPassword());
        
        return supabaseClient.post()
                .uri("/auth/v1/token?grant_type=password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .doOnSuccess(response -> logger.info("Successfully signed in user with email: {}", request.getEmail()))
                .doOnError(error -> logger.error("Error signing in user with email: {}", error.getMessage()));
    }
    
    /**
     * Sign up with phone and password
     */
    public Mono<AuthResponse> signUpWithPhone(PhoneAuthRequest request) {
        logger.info("Signing up user with phone: {}", request.getPhone());
        
        Map<String, Object> body = new HashMap<>();
        body.put("phone", request.getPhone());
        body.put("password", request.getPassword());
        
        return supabaseClient.post()
                .uri("/auth/v1/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .doOnSuccess(response -> logger.info("Successfully signed up user with phone: {}", request.getPhone()))
                .doOnError(error -> logger.error("Error signing up user with phone: {}", error.getMessage()));
    }
    
    /**
     * Sign in with phone and password - requests OTP
     */
    public Mono<Void> signInWithPhone(PhoneAuthRequest request) {
        logger.info("Signing in user with phone: {}", request.getPhone());
        
        Map<String, Object> body = new HashMap<>();
        body.put("phone", request.getPhone());
        body.put("password", request.getPassword());
        
        return supabaseClient.post()
                .uri("/auth/v1/otp")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(response -> logger.info("Successfully initiated OTP for phone: {}", request.getPhone()))
                .doOnError(error -> logger.error("Error initiating OTP: {}", error.getMessage()));
    }
    
    /**
     * Verify phone OTP
     */
    public Mono<AuthResponse> verifyPhoneOtp(OtpVerificationRequest request) {
        logger.info("Verifying OTP for phone: {}", request.getPhone());
        
        Map<String, Object> body = new HashMap<>();
        body.put("phone", request.getPhone());
        body.put("token", request.getToken());
        body.put("type", "sms");
        
        return supabaseClient.post()
                .uri("/auth/v1/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .doOnSuccess(response -> logger.info("Successfully verified OTP for phone: {}", request.getPhone()))
                .doOnError(error -> logger.error("Error verifying OTP: {}", error.getMessage()));
    }
    
    /**
     * Get OAuth URL for Google sign-in
     * @param redirectUrl The URL to redirect to after successful authentication
     * @return URL to redirect the user to for Google sign-in
     */
    public Mono<Map> getGoogleSignInUrl(String redirectUrl) {
        logger.info("Getting Google sign-in URL with redirect: {}", redirectUrl);
        
        return supabaseClient.post()
                .uri("/auth/v1/authorize?provider=google")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("redirect_to", redirectUrl))
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> logger.info("Successfully got Google sign-in URL"))
                .doOnError(error -> logger.error("Error getting Google sign-in URL: {}", error.getMessage()));
    }
    
    /**
     * Exchange OAuth code for session
     * @param code The code returned by the OAuth provider
     * @return Authentication response with token and user info
     */
    public Mono<AuthResponse> exchangeCodeForSession(String code) {
        logger.info("Exchanging OAuth code for session");
        
        return supabaseClient.post()
                .uri("/auth/v1/token?grant_type=authorization_code")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("code", code))
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .doOnSuccess(response -> logger.info("Successfully exchanged code for session"))
                .doOnError(error -> logger.error("Error exchanging code for session: {}", error.getMessage()));
    }
    
    /**
     * Test direct Google OAuth with Supabase
     */
    public Mono<Map> testGoogleOAuth() {
        logger.info("Testing Google OAuth direct call to Supabase");
        
        return supabaseClient.get()
                .uri("/auth/v1/providers")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> logger.info("Successfully got OAuth providers: {}", response))
                .doOnError(error -> logger.error("Error getting OAuth providers: {}", error.getMessage()));
    }
    
    /**
     * Reset a user's password by sending a reset email
     * @param email The email of the user who wants to reset their password
     * @return Empty Mono indicating the operation completed
     */
    public Mono<Void> resetPassword(String email) {
        logger.info("Requesting password reset for email: {}", email);
        
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        
        return supabaseClient.post()
                .uri("/auth/v1/recover")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(response -> logger.info("Successfully sent password reset email to: {}", email))
                .doOnError(error -> logger.error("Error sending password reset email: {}", error.getMessage()));
    }


    public Mono<WalletResponse> getWalletBalance(String userId, String token) {
        logger.info("Getting wallet balance for user: {}", userId);

        // Use admin client to bypass authentication issues
        return supabaseAdminClient.get()
                .uri("/rest/v1/wallets?user_id=eq." + userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Wallet>>() {})
                .flatMap(wallets -> {
                    if (wallets == null || wallets.isEmpty()) {
                        // Wallet doesn't exist, create a new one with admin client
                        logger.info("Wallet not found for user: {}. Creating a new wallet.", userId);
                        return createWalletWithAdmin(userId, 1000.0);
                    } else {
                        // Wallet exists
                        Wallet wallet = wallets.get(0);
                        logger.info("Wallet found for user: {} with balance: {}", userId, wallet.getBalance());
                        return Mono.just(new WalletResponse(wallet));
                    }
                })
                .onErrorResume(error -> {
                    logger.error("Error getting wallet balance: {}", error.getMessage());
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) error;
                        logger.error("Response status: {} - {}", wcre.getStatusCode(), wcre.getStatusText());
                        logger.error("Response body: {}", wcre.getResponseBodyAsString());

                        // If it's a 404, the table might not exist - create it
                        if (wcre.getStatusCode().value() == 404) {
                            return createWalletWithAdmin(userId, 1000.0);
                        }
                    }
                    return Mono.just(new WalletResponse("Error getting wallet balance: " + error.getMessage(), userId, 0.0));
                });
    }
    private Mono<WalletResponse> createWallet(String userId, String token) {
        logger.info("Creating wallet for user: {}", userId);

        Wallet newWallet = new Wallet();
        newWallet.setUserId(userId);
        newWallet.setBalance(1000.0); // Initial balance

        return supabaseClient.post()
                .uri("/rest/v1/wallets")
                .headers(headers -> {
                    // IMPORTANT: These headers must be set correctly
                    headers.set("apikey", supabaseKey);  // Supabase API key
                    headers.set("Authorization", "Bearer " + token);  // User's JWT token
                    headers.set("Prefer", "return=representation");

                    // Log headers for debugging
                    logger.info("Creating wallet with headers - apikey length: {}, token length: {}",
                            supabaseKey.length(),
                            token != null ? token.length() : 0);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newWallet)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Wallet>>() {})
                .map(wallets -> {
                    if (wallets != null && !wallets.isEmpty()) {
                        Wallet createdWallet = wallets.get(0);
                        logger.info("Wallet created successfully with ID: {}", createdWallet.getId());
                        return new WalletResponse(createdWallet);
                    } else {
                        logger.info("Wallet created but no response returned");
                        return new WalletResponse(userId, 1000.0, true);
                    }
                })
                .onErrorResume(error -> {
                    logger.error("Error creating wallet: {}", error.getMessage());
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) error;
                        logger.error("Status: {}, Response: {}", wcre.getStatusCode(), wcre.getResponseBodyAsString());
                        logger.error("Request URL: {}", wcre.getRequest().getURI());
                        logger.error("Request Headers: {}", wcre.getRequest().getHeaders());
                    }
                    return Mono.just(new WalletResponse("Error creating wallet: " + error.getMessage(), userId, 0.0));
                });
    }

    public Mono<WalletResponse> addFunds(String userId, Double amount, String token) {
        logger.info("Adding {} funds to wallet for user: {}", amount, userId);

        // First get the current wallet
        return getWalletBalanceWithAdmin(userId)
                .flatMap(wallet -> {
                    // Calculate new balance
                    double currentBalance = wallet.getBalance() != null ? wallet.getBalance() : 0.0;
                    double newBalance = currentBalance + amount;

                    // Update wallet with new balance using admin client
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("balance", newBalance);

                    return supabaseAdminClient.patch()
                            .uri("/rest/v1/wallets?user_id=eq." + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(updateData)
                            .retrieve()
                            .bodyToMono(String.class)
                            .map(response -> {
                                logger.info("Successfully updated wallet balance: {}", response);
                                return new WalletResponse(userId, newBalance, true);
                            })
                            .onErrorResume(error -> {
                                logger.error("Error updating wallet balance: {}", error.getMessage());
                                if (error instanceof WebClientResponseException) {
                                    WebClientResponseException wcre = (WebClientResponseException) error;
                                    logger.error("Status: {}, Response: {}", wcre.getStatusCode(), wcre.getResponseBodyAsString());

                                    if (wcre.getStatusCode().value() == 404) {
                                        // Wallet not found, create one with initial balance
                                        return createWalletWithAdmin(userId, currentBalance + amount);
                                    }
                                }
                                return Mono.just(new WalletResponse("Error updating balance: " + error.getMessage(), userId, currentBalance));
                            });
                });
    }

    // Helper method to get wallet balance using admin client
    private Mono<WalletResponse> getWalletBalanceWithAdmin(String userId) {
        logger.info("Getting wallet balance with admin client for user: {}", userId);

        return supabaseAdminClient.get()
                .uri("/rest/v1/wallets?user_id=eq." + userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Wallet>>() {})
                .flatMap(wallets -> {
                    if (wallets == null || wallets.isEmpty()) {
                        // Create wallet if it doesn't exist
                        return createWalletWithAdmin(userId, 0.0);
                    } else {
                        Wallet wallet = wallets.get(0);
                        logger.info("Wallet found with admin client for user: {} with balance: {}", userId, wallet.getBalance());
                        return Mono.just(new WalletResponse(wallet));
                    }
                })
                .onErrorResume(error -> {
                    logger.error("Error getting wallet balance with admin client: {}", error.getMessage());
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) error;
                        if (wcre.getStatusCode().value() == 404) {
                            // Table might not exist, create it
                            return createWalletWithAdmin(userId, 0.0);
                        }
                    }
                    return Mono.just(new WalletResponse("Error getting wallet balance: " + error.getMessage(), userId, 0.0));
                });
    }

    // Helper method to create wallet using admin client
    private Mono<WalletResponse> createWalletWithAdmin(String userId, Double initialBalance) {
        logger.info("Creating wallet with admin client for user: {} with initial balance: {}", userId, initialBalance);

        Wallet newWallet = new Wallet();
        newWallet.setUserId(userId);
        newWallet.setBalance(initialBalance != null ? initialBalance : 1000.0);

        return supabaseAdminClient.post()
                .uri("/rest/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newWallet)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Wallet>>() {})
                .map(wallets -> {
                    if (wallets != null && !wallets.isEmpty()) {
                        Wallet createdWallet = wallets.get(0);
                        logger.info("Wallet created successfully with admin client, ID: {}", createdWallet.getId());
                        return new WalletResponse(createdWallet);
                    } else {
                        logger.info("Wallet created with admin client but no response returned");
                        return new WalletResponse(userId, initialBalance, true);
                    }
                })
                .onErrorResume(error -> {
                    logger.error("Error creating wallet with admin client: {}", error.getMessage());
                    return Mono.just(new WalletResponse("Error creating wallet: " + error.getMessage(), userId, 0.0));
                });
    }

    public Mono<WalletResponse> deductFunds(String userId, Double amount, String token) {
        logger.info("Deducting {} funds from wallet for user: {}", amount, userId);

        // First get the current wallet
        return getWalletBalance(userId, token)
                .flatMap(wallet -> {
                    // Check if there are sufficient funds
                    double currentBalance = wallet.getBalance() != null ? wallet.getBalance() : 0.0;
                    if (currentBalance < amount) {
                        return Mono.just(new WalletResponse("Insufficient funds", userId, currentBalance));
                    }

                    // Calculate new balance
                    double newBalance = currentBalance - amount;

                    // Update wallet with new balance
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("balance", newBalance);

                    return supabaseClient.patch()
                            .uri("/rest/v1/wallets?user_id=eq." + userId)
                            .headers(headers -> {
                                headers.set("apikey", supabaseKey);
                                headers.set("Authorization", "Bearer " + token);
                                headers.set("Prefer", "return=representation");
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(updateData)
                            .retrieve()
                            .bodyToMono(String.class)
                            .map(response -> {
                                logger.info("Successfully updated wallet balance: {}", response);
                                return new WalletResponse(userId, newBalance, true);
                            })
                            .onErrorResume(error -> {
                                logger.error("Error updating wallet balance: {}", error.getMessage());
                                return Mono.just(new WalletResponse("Error updating balance: " + error.getMessage(), userId, currentBalance));
                            });
                });
    }

    public Mono<Map<String, Object>> getTransactionHistory(String userId, String token) {
        logger.info("Getting transaction history for user: {}", userId);

        return supabaseClient.get()
                .uri("/rest/v1/transactions?user_id=eq." + userId + "&order=created_at.desc")
                .headers(headers -> {
                    headers.set("apikey", supabaseKey);
                    headers.set("Authorization", "Bearer " + token);
                })
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("transactions", response);
                    result.put("userId", userId);
                    result.put("success", true);
                    return result;
                })
                .onErrorResume(error -> {
                    logger.error("Error getting transaction history: {}", error.getMessage());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", error.getMessage());
                    errorResponse.put("userId", userId);
                    errorResponse.put("success", false);
                    return Mono.just(errorResponse);
                });
    }

//    /**
//     * Get the wallet balance for a user from Supabase
//     * @param userId The ID of the user to get the wallet balance for
//     * @param token Authentication token for the user
//     * @return WalletResponse containing the wallet balance information
//     */
//    public Mono<WalletResponse> getWalletBalance(String userId, String token) {
//        logger.info("Getting wallet balance for user: {}", userId);
//
//        // Check if wallet exists, if not create one
//        return supabaseClient.get()
//                .uri("/rest/v1/wallets?user_id=eq." + userId)
//                .headers(headers -> {
//                    headers.set("apikey", supabaseKey);
//                    headers.set("Authorization", "Bearer " + token);
//                })
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<List<Wallet>>() {})
//                .flatMap(wallets -> {
//                    if (wallets == null || wallets.isEmpty()) {
//                        // Wallet doesn't exist, create a new one
//                        logger.info("Wallet not found for user: {}. Creating a new wallet.", userId);
//                        return createWallet(userId, token);
//                    } else {
//                        // Wallet exists
//                        Wallet wallet = wallets.get(0);
//                        logger.info("Wallet found for user: {} with balance: {}", userId, wallet.getBalance());
//                        return Mono.just(new WalletResponse(wallet));
//                    }
//                })
//                .onErrorResume(error -> {
//                    logger.error("Error getting wallet balance: {}", error.getMessage());
//                    if (error instanceof WebClientResponseException) {
//                        WebClientResponseException wcre = (WebClientResponseException) error;
//                        logger.error("Response status: {} - {}", wcre.getStatusCode(), wcre.getStatusText());
//                        logger.error("Response body: {}", wcre.getResponseBodyAsString());
//
//                        // If wallet table doesn't exist or any other error, create one
//                        if (wcre.getStatusCode().is4xxClientError()) {
//                            return createWallet(userId, token);
//                        }
//                    }
//                    return Mono.just(new WalletResponse("Error getting wallet balance: " + error.getMessage(), userId, 0.0));
//                });
//    }
//
//    private Mono<WalletResponse> createWallet(String userId, String token) {
//        logger.info("Creating wallet for user: {}", userId);
//
//        Wallet newWallet = new Wallet();
//        newWallet.setUserId(userId);
//        newWallet.setBalance(1000.0); // Initial balance
//
//        return supabaseClient.post()
//                .uri("/rest/v1/wallets")
//                .headers(headers -> {
//                    headers.set("apikey", supabaseKey);
//                    headers.set("Authorization", "Bearer " + token);
//                    headers.set("Prefer", "return=representation");
//                })
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(newWallet)
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<List<Wallet>>() {})
//                .map(wallets -> {
//                    if (wallets != null && !wallets.isEmpty()) {
//                        Wallet createdWallet = wallets.get(0);
//                        logger.info("Wallet created successfully with ID: {}", createdWallet.getId());
//                        return new WalletResponse(createdWallet);
//                    } else {
//                        logger.info("Wallet created but no response returned");
//                        return new WalletResponse(userId, 1000.0, true);
//                    }
//                })
//                .onErrorResume(error -> {
//                    logger.error("Error creating wallet: {}", error.getMessage());
//                    if (error instanceof WebClientResponseException) {
//                        WebClientResponseException wcre = (WebClientResponseException) error;
//                        logger.error("Status: {}, Response: {}", wcre.getStatusCode(), wcre.getResponseBodyAsString());
//                    }
//                    return Mono.just(new WalletResponse("Error creating wallet: " + error.getMessage(), userId, 0.0));
//                });
//    }

    /**
     * Explicitly create a wallet for a user with a specified initial balance
     * @param userId User ID
     * @param initialBalance Initial balance for the wallet
     * @param token Authentication token
     * @return Created wallet information
     */
    public Mono<WalletResponse> createWalletExplicitly(String userId, Double initialBalance, String token) {
        logger.info("Explicitly creating wallet for user: {} with initial balance: {}", userId, initialBalance);

        // First check if wallet already exists
        return supabaseClient.get()
                .uri("/rest/v1/wallets?user_id=eq." + userId)
                .headers(headers -> {
                    headers.set("apikey", supabaseKey);
                    headers.set("Authorization", "Bearer " + token);
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Wallet>>() {})
                .flatMap(wallets -> {
                    if (wallets != null && !wallets.isEmpty()) {
                        // Wallet already exists
                        Wallet existingWallet = wallets.get(0);
                        logger.info("Wallet already exists for user: {}", userId);
                        return Mono.just(new WalletResponse(existingWallet.getUserId(), existingWallet.getBalance(), false, "Wallet already exists for this user"));
                    }

                    // Create new wallet
                    Wallet newWallet = new Wallet();
                    newWallet.setUserId(userId);
                    newWallet.setBalance(initialBalance);

                    return supabaseClient.post()
                            .uri("/rest/v1/wallets")
                            .headers(headers -> {
                                headers.set("apikey", supabaseKey);
                                headers.set("Authorization", "Bearer " + token);
                                headers.set("Prefer", "return=representation");
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(newWallet)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<Wallet>>() {})
                            .map(createdWallets -> {
                                if (createdWallets != null && !createdWallets.isEmpty()) {
                                    Wallet createdWallet = createdWallets.get(0);
                                    logger.info("Wallet created successfully with ID: {}", createdWallet.getId());
                                    return new WalletResponse(createdWallet.getUserId(), createdWallet.getBalance(), true, "Wallet created successfully");
                                } else {
                                    logger.info("Wallet created but no response returned");
                                    return new WalletResponse(userId, initialBalance, true, "Wallet created successfully");
                                }
                            })
                            .onErrorResume(error -> {
                                logger.error("Error creating wallet: {}", error.getMessage());
                                if (error instanceof WebClientResponseException) {
                                    WebClientResponseException wcre = (WebClientResponseException) error;
                                    logger.error("Status: {}, Response: {}", wcre.getStatusCode(), wcre.getResponseBodyAsString());
                                }
                                return Mono.just(new WalletResponse("Error creating wallet: " + error.getMessage(), userId, 0.0));
                            });
                })
                .onErrorResume(error -> {
                    logger.error("Error checking existing wallet: {}", error.getMessage());
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) error;
                        logger.error("Status: {}, Response: {}", wcre.getStatusCode(), wcre.getResponseBodyAsString());

                        // If the error is because table doesn't exist
                        if (wcre.getStatusCode().value() == 404 &&
                                wcre.getResponseBodyAsString().contains("relation") &&
                                wcre.getResponseBodyAsString().contains("does not exist")) {

                            logger.info("Tables don't exist, attempting to create them");
                            return ensureTablesExist(token)
                                    .flatMap(tablesCreated -> {
                                        if (tablesCreated) {
                                            Wallet newWallet = new Wallet();
                                            newWallet.setUserId(userId);
                                            newWallet.setBalance(initialBalance);

                                            return supabaseClient.post()
                                                    .uri("/rest/v1/wallets")
                                                    .headers(headers -> {
                                                        headers.set("apikey", supabaseKey);
                                                        headers.set("Authorization", "Bearer " + token);
                                                        headers.set("Prefer", "return=representation");
                                                    })
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .bodyValue(newWallet)
                                                    .retrieve()
                                                    .bodyToMono(new ParameterizedTypeReference<List<Wallet>>() {})
                                                    .map(createdWallets -> {
                                                        if (createdWallets != null && !createdWallets.isEmpty()) {
                                                            Wallet createdWallet = createdWallets.get(0);
                                                            logger.info("Wallet created successfully with ID: {}", createdWallet.getId());
                                                            return new WalletResponse(createdWallet.getUserId(), createdWallet.getBalance(), true, "Wallet created successfully");
                                                        } else {
                                                            logger.info("Wallet created but no response returned");
                                                            return new WalletResponse(userId, initialBalance, true, "Wallet created successfully");
                                                        }
                                                    })
                                                    .onErrorResume(createError -> {
                                                        logger.error("Error creating wallet after table creation: {}", createError.getMessage());
                                                        return Mono.just(new WalletResponse("Error creating wallet: " + createError.getMessage(), userId, 0.0));
                                                    });
                                        } else {
                                            return Mono.just(new WalletResponse("Failed to create required database tables", userId, 0.0));
                                        }
                                    });
                        }
                    }
                    return Mono.just(new WalletResponse("Error checking existing wallet: " + error.getMessage(), userId, 0.0));
                });
    }

    /**
     * Check if a wallet exists for a user
     * @param userId User ID
     * @param token Authentication token
     * @return Map containing wallet information
     */
    public Mono<Map<String, Object>> checkWalletExists(String userId, String token) {
        logger.info("Checking if wallet exists for user: {}", userId);

        return supabaseClient.get()
                .uri("/rest/v1/wallets?user_id=eq." + userId)
                .headers(headers -> {
                    headers.set("apikey", supabaseKey);
                    headers.set("Authorization", "Bearer " + token);
                })
                .retrieve()
                .bodyToMono(String.class)
                .map(result -> {
                    Map<String, Object> response = new HashMap<>();
                    boolean exists = !result.equals("[]") && !result.contains("\"userId\":null");
                    response.put("exists", exists);
                    response.put("userId", userId);

                    if (exists) {
                        response.put("data", result);
                    }

                    return response;
                })
                .onErrorResume(error -> {
                    logger.error("Error checking wallet existence: {}", error.getMessage());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", error.getMessage());
                    errorResponse.put("exists", false);
                    errorResponse.put("userId", userId);
                    return Mono.just(errorResponse);
                });
    }

    /**
     * Ensure that the necessary tables exist in Supabase
     * @param token Authentication token
     * @return Mono<Boolean> indicating if tables are ready
     */
    public Mono<Boolean> ensureTablesExist(String token) {
        logger.info("Ensuring necessary tables exist in Supabase");

        // First, check if wallets table exists
        return checkTableExists("wallets", token)
                .flatMap(walletsExists -> {
                    if (!walletsExists) {
                        // Create wallets table
                        logger.info("Creating wallets table");
                        return createWalletsTable(token)
                                // Then check/create transactions table
                                .then(checkTableExists("transactions", token))
                                .flatMap(transactionsExists -> {
                                    if (!transactionsExists) {
                                        logger.info("Creating transactions table");
                                        return createTransactionsTable(token).thenReturn(true);
                                    }
                                    return Mono.just(true);
                                });
                    } else {
                        // Wallets table exists, check transactions table
                        return checkTableExists("transactions", token)
                                .flatMap(transactionsExists -> {
                                    if (!transactionsExists) {
                                        logger.info("Creating transactions table");
                                        return createTransactionsTable(token).thenReturn(true);
                                    }
                                    return Mono.just(true);
                                });
                    }
                })
                .onErrorResume(error -> {
                    logger.error("Error ensuring tables exist: {}", error.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * Check if a table exists in Supabase
     * @param tableName Table name to check
     * @param token Authentication token
     * @return Mono<Boolean> indicating if table exists
     */
    private Mono<Boolean> checkTableExists(String tableName, String token) {
        return supabaseClient.get()
                .uri("/rest/v1/" + tableName + "?limit=1")
                .headers(headers -> {
                    headers.set("apikey", supabaseKey);
                    headers.set("Authorization", "Bearer " + token);
                })
                .retrieve()
                .bodyToMono(String.class)
                .map(result -> true)
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) error;
                        if (wcre.getStatusCode().is4xxClientError()) {
                            logger.info("Table '{}' does not exist", tableName);
                            return Mono.just(false);
                        }
                    }
                    logger.error("Error checking if table '{}' exists: {}", tableName, error.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * Create the wallets table in Supabase
     * @param token Authentication token
     * @return Mono<Void> indicating completion
     */
    private Mono<Void> createWalletsTable(String token) {
        // Using the service role key is recommended for table creation
        return supabaseAdminClient.post()
                .uri("/rest/v1/rpc/create_wallets_table")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of())
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(error -> {
                    logger.error("Error creating wallets table: {}", error.getMessage());
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) error;
                        logger.error("Status: {}, Response: {}", wcre.getStatusCode(), wcre.getResponseBodyAsString());
                    }
                    return Mono.empty();
                });
    }

    /**
     * Create the transactions table in Supabase
     * @param token Authentication token
     * @return Mono<Void> indicating completion
     */
    private Mono<Void> createTransactionsTable(String token) {
        // Using the service role key is recommended for table creation
        return supabaseAdminClient.post()
                .uri("/rest/v1/rpc/create_transactions_table")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of())
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(error -> {
                    logger.error("Error creating transactions table: {}", error.getMessage());
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) error;
                        logger.error("Status: {}, Response: {}", wcre.getStatusCode(), wcre.getResponseBodyAsString());
                    }
                    return Mono.empty();
                });
    }
} 