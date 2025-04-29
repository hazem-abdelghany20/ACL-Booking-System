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
import com.example.hotel.UserAuthService.security.services.SupabaseAuthService;
import com.example.hotel.UserAuthService.security.services.UserDetailsImpl;

import io.supabase.data.auth.UserResponse;

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
            // Authenticate with Supabase
            UserResponse userResponse = supabaseAuthService.signIn(loginRequest.getUsername(), loginRequest.getPassword());
            
            // Also authenticate locally
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Return the Supabase authentication response
            return ResponseEntity.ok(userResponse);
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
            // Register with Supabase
            UserResponse userResponse = supabaseAuthService.signUp(
                    signUpRequest.getEmail(), 
                    signUpRequest.getPassword(), 
                    signUpRequest.getUsername()
            );
            
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser(@RequestBody String accessToken) {
        try {
            supabaseAuthService.signOut(accessToken);
            return ResponseEntity.ok(new MessageResponse("You've been signed out!"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody String email) {
        try {
            supabaseAuthService.resetPassword(email);
            return ResponseEntity.ok(new MessageResponse("Password reset link sent!"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
} 