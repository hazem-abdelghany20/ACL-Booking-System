package com.example.hotel.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.hotel.apigateway.filter.JwtAuthenticationFilter;

@Configuration
public class GatewaySecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator secureRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // User routes that require authentication
                .route("secure-user-routes", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://user-auth-service:8080"))

                // Booking routes that require authentication
                .route("secure-booking-routes", r -> r
                        .path("/api/bookings/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://booking-service:8090"))

                // Events routes that require authentication (creating/updating events)
                .route("secure-event-routes", r -> r
                        .path("/api/events/**")
                        .and()
                        .method("POST", "PUT", "DELETE", "PATCH")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://event-service:8081"))

                // Notification routes that require authentication
                .route("secure-notification-routes", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://notification-service:8092"))

                .build();
    }
}