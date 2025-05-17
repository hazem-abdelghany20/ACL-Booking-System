package com.example.hotel.UserAuthService.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.hotel.UserAuthService.models.User;
import com.example.hotel.UserAuthService.payload.response.MessageResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UserController with a placeholder implementation for Supabase integration.
 * This controller provides API endpoints for user management.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Placeholder for in-memory user storage
    private static final Map<Long, User> users = new HashMap<>();
    
    // Initialize with some sample data
    static {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("admin");
        user2.setEmail("admin@example.com");
        
        users.put(1L, user1);
        users.put(2L, user2);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('EVENT_ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = Optional.ofNullable(users.get(id));
        return user
                .map(u -> {
                    // Clear password for security reasons before returning
                    u.setPassword("");
                    return ResponseEntity.ok(u);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (users.containsKey(id)) {
            users.remove(id);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
    
    // Test endpoints for role-based authorization
    
    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }
    
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('EVENT_ORGANIZER') or hasRole('ADMIN')")
    public String userAccess() {
        return "User Content.";
    }
    
    @GetMapping("/organizer")
    @PreAuthorize("hasRole('EVENT_ORGANIZER')")
    public String organizerAccess() {
        return "Event Organizer Board.";
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }
} 