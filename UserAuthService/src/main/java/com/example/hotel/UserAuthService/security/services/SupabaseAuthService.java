package com.example.hotel.UserAuthService.security.services;

import com.example.hotel.UserAuthService.models.ERole;
import com.example.hotel.UserAuthService.models.Role;
import com.example.hotel.UserAuthService.models.User;
import com.example.hotel.UserAuthService.repositories.RoleRepository;
import com.example.hotel.UserAuthService.repositories.UserRepository;
import io.supabase.SupabaseClient;
import io.supabase.data.auth.UserAttributes;
import io.supabase.data.auth.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class SupabaseAuthService {
    
    @Autowired
    private SupabaseClient supabaseClient;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public UserResponse signIn(String email, String password) throws Exception {
        try {
            return supabaseClient.auth().signIn(email, password);
        } catch (Exception e) {
            throw new Exception("Authentication failed: " + e.getMessage());
        }
    }
    
    public UserResponse signUp(String email, String password, String username) throws Exception {
        try {
            // Register with Supabase
            UserResponse userResponse = supabaseClient.auth().signUp(email, password);
            
            // If Supabase registration is successful, also save to our database
            if (userResponse != null && userResponse.getUser() != null) {
                // Check if user already exists in our local DB
                if (!userRepository.existsByEmail(email)) {
                    User user = new User(username, email, passwordEncoder.encode(password));
                    
                    // Set default role as USER
                    Set<Role> roles = new HashSet<>();
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
                    roles.add(userRole);
                    user.setRoles(roles);
                    
                    userRepository.save(user);
                }
            }
            
            return userResponse;
        } catch (Exception e) {
            throw new Exception("Registration failed: " + e.getMessage());
        }
    }
    
    public void signOut(String accessToken) throws Exception {
        try {
            supabaseClient.auth().signOut(accessToken);
        } catch (Exception e) {
            throw new Exception("Sign out failed: " + e.getMessage());
        }
    }
    
    public UserResponse updateUser(String accessToken, String email, String password, String username) throws Exception {
        try {
            UserAttributes attributes = new UserAttributes.Builder()
                    .email(email)
                    .password(password)
                    .data(Collections.singletonMap("username", username))
                    .build();
            
            UserResponse response = supabaseClient.auth().updateUser(accessToken, attributes);
            
            // Also update in our database
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setUsername(username);
                if (password != null && !password.isEmpty()) {
                    user.setPassword(passwordEncoder.encode(password));
                }
                userRepository.save(user);
            }
            
            return response;
        } catch (Exception e) {
            throw new Exception("Update user failed: " + e.getMessage());
        }
    }
    
    public void resetPassword(String email) throws Exception {
        try {
            supabaseClient.auth().resetPasswordForEmail(email);
        } catch (Exception e) {
            throw new Exception("Password reset failed: " + e.getMessage());
        }
    }
} 