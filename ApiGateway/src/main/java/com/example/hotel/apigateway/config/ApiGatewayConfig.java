package com.example.hotel.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class ApiGatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Auth Service Routes
                .route("user-auth-service", r -> r
                        .path("/api/auth/**", "/api/oauth/**", "/api/supabase/**", "/api/users/**")
                        .uri("http://user-auth-service:8080"))

                // Event Service Routes
                .route("event-service", r -> r
                        .path("/api/events/**", "/api/categories/**")
                        .uri("http://event-service:8081"))

                // Booking Service Routes
                .route("booking-service", r -> r
                        .path("/api/bookings/**")
                        .uri("http://booking-service:8090"))

                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("http://notification-service:8092"))

                // Static resources (for the frontend client app)
                .route("static-resources", r -> r
                        .path("/static/**")
                        .uri("http://user-auth-service:8080"))

                // Frontend routes (Single Page Application support)
                .route("frontend-fallback", r -> r
                        .path("/**")
                        .and()
                        .method(HttpMethod.GET)
                        .uri("http://user-auth-service:8080"))

                .build();
    }
}