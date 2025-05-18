package com.example.hotel.UserAuthService.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    
    @Bean
    public BasicAuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("api realm");
        return entryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> 
                        // Public endpoints
                        auth.requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/api/oauth/**").permitAll()
                            .requestMatchers("/api/users/all").permitAll()
                            .requestMatchers("/google-login").permitAll()
                            .requestMatchers("/auth-success").permitAll()
                            .requestMatchers("/auth-error").permitAll()
                            .requestMatchers("/static/**").permitAll()
                            // Protected wallet endpoints - require authentication
                            .requestMatchers("/api/wallet/**").authenticated()

                            .anyRequest().authenticated()
                );
        
        return http.build();
    }
} 