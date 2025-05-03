package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import com.example.hotel.UserAuthService.payload.request.EmailAuthRequest;
import com.example.hotel.UserAuthService.payload.request.OtpVerificationRequest;
import com.example.hotel.UserAuthService.payload.request.PhoneAuthRequest;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/supabase")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SupabaseAuthController {
    
    private final SupabaseAuthService supabaseAuthService;
    
    public SupabaseAuthController(SupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }
    
    @PostMapping("/signup/email")
    public Mono<ResponseEntity<AuthResponse>> signUpWithEmail(@Valid @RequestBody EmailAuthRequest request) {
        return supabaseAuthService.signUpWithEmail(request)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
    
    @PostMapping("/signin/email")
    public Mono<ResponseEntity<AuthResponse>> signInWithEmail(@Valid @RequestBody EmailAuthRequest request) {
        return supabaseAuthService.signInWithEmail(request)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    @PostMapping("/signup/phone")
    public Mono<ResponseEntity<AuthResponse>> signUpWithPhone(@Valid @RequestBody PhoneAuthRequest request) {
        return supabaseAuthService.signUpWithPhone(request)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
    
    @PostMapping("/signin/phone")
    public Mono<ResponseEntity<Void>> signInWithPhone(@Valid @RequestBody PhoneAuthRequest request) {
        return supabaseAuthService.signInWithPhone(request)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    @PostMapping("/verify-otp")
    public Mono<ResponseEntity<AuthResponse>> verifyPhoneOtp(@Valid @RequestBody OtpVerificationRequest request) {
        return supabaseAuthService.verifyPhoneOtp(request)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
} 