package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.auth.service.AuthService;
import com.example.hotel.UserAuthService.payload.request.EmailAuthRequest;
import com.example.hotel.UserAuthService.payload.request.ResetPasswordRequest;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import com.example.hotel.UserAuthService.payload.response.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller for authentication endpoints using the new AuthService
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SupabaseAuthController {
    
    private final AuthService authService;
    
    public SupabaseAuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/signup")
    public Mono<ResponseEntity<AuthResponse>> signUp(@Valid @RequestBody EmailAuthRequest request) {
        return authService.signUpWithEmail(request)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
    
    @PostMapping("/signin")
    public Mono<ResponseEntity<AuthResponse>> signIn(@Valid @RequestBody EmailAuthRequest request) {

       System.out.println(request);
        return authService.signInWithEmail(request)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        // Extract user ID from token or session
        String userId = null;
        if (token != null && !token.isEmpty()) {
            // Find the session with this token
            userId = authService.getUserIdFromToken(token);
            if (userId != null) {
                authService.signOut(userId);
                return ResponseEntity.ok(new MessageResponse("You've been signed out!"));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid token"));
    }
    
    @GetMapping("/validate")
    public ResponseEntity<?> validateSession(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userId = authService.getUserIdFromToken(token);
        if (userId != null) {
            boolean isValid = authService.validateSession(userId);
            return ResponseEntity.ok(new MessageResponse(isValid ? "Session is valid" : "Session is invalid"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid token"));
    }
    
    @PostMapping("/reset-password")
    public Mono<ResponseEntity<MessageResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request.getEmail())
                .map(result -> ResponseEntity.ok(new MessageResponse("Password reset email sent successfully")))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Failed to send password reset email")));
    }
} 