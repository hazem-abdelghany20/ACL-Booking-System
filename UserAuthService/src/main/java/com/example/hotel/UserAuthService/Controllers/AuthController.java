package com.example.hotel.UserAuthService.Controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hotel.UserAuthService.models.ERole;
import com.example.hotel.UserAuthService.models.Role;
import com.example.hotel.UserAuthService.models.User;
import com.example.hotel.UserAuthService.payload.request.LoginRequest;
import com.example.hotel.UserAuthService.payload.request.SignupRequest;
import com.example.hotel.UserAuthService.payload.response.MessageResponse;
import com.example.hotel.UserAuthService.repositories.RoleRepository;
import com.example.hotel.UserAuthService.repositories.UserRepository;
import com.example.hotel.UserAuthService.security.services.UserDetailsImpl;
import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import com.example.hotel.UserAuthService.payload.request.EmailAuthRequest;
import com.example.hotel.UserAuthService.payload.request.OtpVerificationRequest;
import com.example.hotel.UserAuthService.payload.request.PhoneAuthRequest;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;
    
    @Autowired
    SupabaseAuthService supabaseAuthService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate locally
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Return basic success response
            return ResponseEntity.ok(new MessageResponse("User signed in successfully!"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // Validate username and email
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        try {
            // Create new user's account with basic information
            User user = new User(signUpRequest.getUsername(), 
                                 signUpRequest.getEmail(),
                                 encoder.encode(signUpRequest.getPassword()));
            
            // Save user
            userRepository.save(user);
            
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("You've been signed out!"));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody String email) {
        // Basic placeholder for password reset
        return ResponseEntity.ok(new MessageResponse("Password reset functionality not implemented yet"));
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