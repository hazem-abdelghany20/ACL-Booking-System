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
import reactor.core.publisher.Mono;

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
    
    /**
     * Get the wallet balance for a user from Supabase
     * @param userId The ID of the user to get the wallet balance for
     * @param token Authentication token for the user
     * @return WalletResponse containing the wallet balance information
     */
    public Mono<WalletResponse> getWalletBalance(String userId, String token) {
        logger.info("Getting wallet balance for user: {}", userId);
        
        return supabaseClient.get()
                .uri("/rest/v1/wallets?user_id=eq." + userId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(WalletResponse.class)
                .doOnSuccess(response -> logger.info("Successfully got wallet balance for user: {}", userId))
                .doOnError(error -> logger.error("Error getting wallet balance: {}", error.getMessage()));
    }
    
    /**
     * Add funds to a user's wallet in Supabase
     * @param userId The ID of the user to add funds to
     * @param amount The amount to add
     * @param token Authentication token for the user
     * @return WalletResponse containing the updated wallet information
     */
    public Mono<WalletResponse> addFunds(String userId, Double amount, String token) {
        logger.info("Adding {} funds to wallet for user: {}", amount, userId);
        
        return supabaseClient.post()
                .uri("/rest/v1/rpc/add_funds")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("user_id", userId, "amount", amount))
                .retrieve()
                .bodyToMono(WalletResponse.class)
                .doOnSuccess(response -> logger.info("Successfully added funds to wallet for user: {}", userId))
                .doOnError(error -> logger.error("Error adding funds to wallet: {}", error.getMessage()));
    }
    
    /**
     * Deduct funds from a user's wallet in Supabase
     * @param userId The ID of the user to deduct funds from
     * @param amount The amount to deduct
     * @param token Authentication token for the user
     * @return WalletResponse containing the updated wallet information
     */
    public Mono<WalletResponse> deductFunds(String userId, Double amount, String token) {
        logger.info("Deducting {} funds from wallet for user: {}", amount, userId);
        
        return supabaseClient.post()
                .uri("/rest/v1/rpc/deduct_funds")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("user_id", userId, "amount", amount))
                .retrieve()
                .bodyToMono(WalletResponse.class)
                .doOnSuccess(response -> logger.info("Successfully deducted funds from wallet for user: {}", userId))
                .doOnError(error -> logger.error("Error deducting funds from wallet: {}", error.getMessage()));
    }
    
    /**
     * Get transaction history for a user from Supabase
     * @param userId The ID of the user to get transaction history for
     * @param token Authentication token for the user
     * @return Map containing transaction history
     */
    public Mono<Map> getTransactionHistory(String userId, String token) {
        logger.info("Getting transaction history for user: {}", userId);
        
        return supabaseClient.get()
                .uri("/rest/v1/transactions?user_id=eq." + userId + "&order=created_at.desc")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> logger.info("Successfully got transaction history for user: {}", userId))
                .doOnError(error -> logger.error("Error getting transaction history: {}", error.getMessage()));
    }
} 