package com.example.hotel.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())  // Explicitly disable CSRF for REST APIs
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/**", "/api/oauth/**").permitAll()
                        .anyExchange().authenticated())
                .build();
    }
}