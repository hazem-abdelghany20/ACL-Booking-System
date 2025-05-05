package com.example.hotel.UserAuthService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.secret}")
    private String supabaseSecret;

    @Bean
    public WebClient supabaseClient() {
        return WebClient.builder()
            .baseUrl(supabaseUrl)
            .defaultHeader("apikey", supabaseKey)
            .defaultHeader("Authorization", "Bearer " + supabaseKey)
            .build();
    }
    
    @Bean
    public WebClient supabaseAdminClient() {
        return WebClient.builder()
            .baseUrl(supabaseUrl)
            .defaultHeader("apikey", supabaseKey)
            .defaultHeader("Authorization", "Bearer " + supabaseSecret)
            .build();
    }
} 