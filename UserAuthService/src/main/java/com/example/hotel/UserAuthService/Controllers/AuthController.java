package com.example.hotel.UserAuthService.Controllers;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import com.example.hotel.UserAuthService.payload.request.EmailAuthRequest;
import com.example.hotel.UserAuthService.payload.request.LoginRequest;
import com.example.hotel.UserAuthService.payload.request.SignupRequest;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import com.example.hotel.UserAuthService.payload.response.MessageResponse;

import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

/**
 * Controller for authentication endpoints. Uses Supabase for all authentication operations.
 * This controller is disabled in favor of SupabaseAuthController.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
// Remove @RestController and @RequestMapping to disable this controller
public class AuthController {
    @Autowired
    private SupabaseAuthService supabaseAuthService;

    @PostMapping("/signin")
    public Mono<ResponseEntity<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Convert to email auth request
        EmailAuthRequest emailRequest = new EmailAuthRequest();
        emailRequest.setEmail(loginRequest.getUsername());
        emailRequest.setPassword(loginRequest.getPassword());
        
        return supabaseAuthService.signInWithEmail(emailRequest)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<AuthResponse>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // Convert to email auth request
        EmailAuthRequest emailRequest = new EmailAuthRequest();
        emailRequest.setEmail(signUpRequest.getEmail());
        emailRequest.setPassword(signUpRequest.getPassword());
        
        return supabaseAuthService.signUpWithEmail(emailRequest)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
    
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok(new MessageResponse("You've been signed out!"));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody String email) {
        // Basic placeholder for password reset
        return ResponseEntity.ok(new MessageResponse("Password reset functionality not implemented yet"));
    }

} 