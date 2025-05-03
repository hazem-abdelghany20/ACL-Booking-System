package com.example.hotel.UserAuthService.Services;

import com.example.hotel.UserAuthService.payload.request.EmailAuthRequest;
import com.example.hotel.UserAuthService.payload.request.OtpVerificationRequest;
import com.example.hotel.UserAuthService.payload.request.PhoneAuthRequest;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
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

@Service
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
     * Sign up with phone number (sends OTP)
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
                .doOnSuccess(response -> logger.info("Successfully initiated phone signup with OTP for: {}", request.getPhone()))
                .doOnError(error -> logger.error("Error signing up user with phone: {}", error.getMessage()));
    }
    
    /**
     * Verify phone OTP code
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
     * Sign in with phone (sends OTP)
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
                .doOnSuccess(response -> logger.info("Successfully sent OTP for phone login: {}", request.getPhone()))
                .doOnError(error -> logger.error("Error sending OTP for phone login: {}", error.getMessage()));
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
} 