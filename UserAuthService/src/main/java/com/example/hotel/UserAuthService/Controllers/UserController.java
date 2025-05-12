package com.example.hotel.UserAuthService.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.hotel.UserAuthService.models.User;
import com.example.hotel.UserAuthService.payload.response.MessageResponse;
import com.example.hotel.UserAuthService.repositories.UserRepository;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    UserRepository userRepository;
    
    @GetMapping
//  @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('EVENT_ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    // Clear password for security reasons before returning
                    user.setPassword("");
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
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