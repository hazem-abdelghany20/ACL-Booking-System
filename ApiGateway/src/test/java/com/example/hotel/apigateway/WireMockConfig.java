package com.example.hotel.apigateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import com.github.tomakehurst.wiremock.WireMockServer;

@TestConfiguration
public class WireMockConfig {

    @Bean
    public RouteLocator testRouteLocator(RouteLocatorBuilder builder, @Autowired WireMockServer wireMockServer) {
        String wireMockUrl = "http://localhost:" + wireMockServer.port();
        
        return builder.routes()
                .route("user-auth-service", r -> r
                        .path("/api/auth/**", "/api/oauth/**", "/api/supabase/**", "/api/users/**")
                        .uri(wireMockUrl))
                .route("event-service", r -> r
                        .path("/api/events/**", "/api/categories/**")
                        .uri(wireMockUrl))
                .route("booking-service", r -> r
                        .path("/api/bookings/**")
                        .uri(wireMockUrl))
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri(wireMockUrl))
                .route("search-service", r -> r
                        .path("/api/search/**")
                        .uri(wireMockUrl))
                .build();
    }
} 