package com.example.hotel.UserAuthService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
                .filter(logRequest()) // Add logging for debugging
                .build();
    }

    @Bean
    public WebClient supabaseAdminClient() {
        return WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", supabaseKey)
                .defaultHeader("Authorization", "Bearer " + supabaseSecret)
                .filter(logRequest()) // Add logging for debugging
                .build();
    }

    // Log filter to help with debugging
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    values.forEach(value -> {
                        if (name.equals("Authorization")) {
                            System.out.println(name + ": Bearer " + value.substring(0, 10) + "...");
                        } else {
                            System.out.println(name + ": " + value);
                        }
                    }));
            return Mono.just(clientRequest);
        });
    }
}