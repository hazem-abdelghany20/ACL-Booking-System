package com.example.hotel.UserAuthService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.cors.CorsConfigurationSource;
import org.springframework.security.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth/**").permitAll()
                .requestMatchers("/api/test/**").permitAll()
                .requestMatchers("/api/wallet/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new org.springframework.security.config.annotation.web.configuration.CorsConfigurationSource() {
            @Override
            public org.springframework.security.config.annotation.web.configuration.CorsConfiguration getCorsConfiguration(org.springframework.web.cors.CorsRequest request) {
                org.springframework.security.config.annotation.web.configuration.CorsConfiguration config = new org.springframework.security.config.annotation.web.configuration.CorsConfiguration();
                config.setAllowedOrigins(java.util.Arrays.asList("*"));
                config.setAllowedMethods(java.util.Arrays.asList("*"));
                config.setAllowedHeaders(java.util.Arrays.asList("*"));
                config.setAllowCredentials(true);
                return config;
            }
        });
        return source;
    }
} 