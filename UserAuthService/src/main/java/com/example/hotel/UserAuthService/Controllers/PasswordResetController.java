package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.payload.PasswordResetEmailRequest;
import com.example.hotel.UserAuthService.payload.PasswordResetConfirmRequest;
import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    private final SupabaseAuthService supabaseAuthService;

    @Autowired
    public PasswordResetController(SupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }

    @PostMapping("/request")
    public Mono<ResponseEntity<String>> requestPasswordReset(@RequestBody PasswordResetEmailRequest request) {
        return supabaseAuthService.requestPasswordReset(request.getEmail())
                .then(Mono.just(ResponseEntity.ok("Password reset email sent. Please check your inbox.")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Error requesting password reset: " + e.getMessage())));
    }

    @PostMapping("/confirm")
    public Mono<ResponseEntity<String>> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        // Assuming request.getToken() is the user's access token after Supabase verification flow
        return supabaseAuthService.updateUserPasswordWithToken(request.getToken(), request.getNewPassword())
                .map(authResponse -> ResponseEntity.ok("Password has been reset successfully."))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Error confirming password reset: " + e.getMessage())));
    }
} 