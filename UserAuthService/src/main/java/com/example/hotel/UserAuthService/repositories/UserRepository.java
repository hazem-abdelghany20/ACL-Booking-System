package com.example.hotel.UserAuthService.repositories;

import java.util.Optional;

import com.example.hotel.UserAuthService.models.User;

/**
 * Placeholder interface for JPA repository.
 * This will be replaced with Supabase integration later.
 */
public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    User save(User user);
} 