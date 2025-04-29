package com.example.hotel.UserAuthService.config;

import io.supabase.SupabaseClient;
import io.supabase.SupabaseClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.secret}")
    private String supabaseSecret;

    @Bean
    public SupabaseClient supabaseClient() {
        SupabaseClientOptions options = new SupabaseClientOptions.Builder()
                .setServiceKey(supabaseSecret)
                .build();
        
        return new SupabaseClient(supabaseUrl, supabaseKey, options);
    }
} 