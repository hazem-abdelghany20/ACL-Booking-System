package com.example.hotel.UserAuthService.repositories;

import com.example.hotel.UserAuthService.models.ERole;
import com.example.hotel.UserAuthService.models.Role;
import com.example.hotel.UserAuthService.models.User;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Placeholder implementation of UserRepository using in-memory storage.
 * This will be replaced with actual Supabase integration.
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    // In-memory storage
    private static final Map<Long, User> users = new HashMap<>();
    private static final Map<String, User> usersByUsername = new HashMap<>();
    private static final Map<String, User> usersByEmail = new HashMap<>();
    private static Long nextId = 3L;

    // Initialize with some test data
    static {
        // Create roles
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(new Role(1, ERole.ROLE_USER));
        
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(new Role(1, ERole.ROLE_USER));
        adminRoles.add(new Role(2, ERole.ROLE_ADMIN));
        
        // Create users
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user");
        user1.setEmail("user@example.com");
        user1.setPassword("$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a"); // "password" encrypted
        user1.setRoles(userRoles);
        
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("admin");
        user2.setEmail("admin@example.com");
        user2.setPassword("$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a"); // "password" encrypted
        user2.setRoles(adminRoles);
        
        // Add to maps
        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);
        usersByUsername.put(user1.getUsername(), user1);
        usersByUsername.put(user2.getUsername(), user2);
        usersByEmail.put(user1.getEmail(), user1);
        usersByEmail.put(user2.getEmail(), user2);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Boolean existsByUsername(String username) {
        return usersByUsername.containsKey(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return usersByEmail.containsKey(email);
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(nextId++);
        }
        
        users.put(user.getId(), user);
        usersByUsername.put(user.getUsername(), user);
        usersByEmail.put(user.getEmail(), user);
        
        return user;
    }
} 