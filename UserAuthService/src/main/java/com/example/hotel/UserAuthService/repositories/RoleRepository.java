package com.example.hotel.UserAuthService.repositories;

import com.example.hotel.UserAuthService.models.ERole;
import com.example.hotel.UserAuthService.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
} 