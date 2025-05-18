package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Import(OAuthTestSecurityConfig.class)
public class OAuthControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private SupabaseAuthService supabaseAuthService;

    @Test
    @WithMockUser(roles = {"USER"})
    public void testGetGoogleSignInUrl() {
        // Prepare mock response
        Map<String, String> mockResponse = new HashMap<>();
        mockResponse.put("url", "https://accounts.google.com/o/oauth2/auth?some-params");

        when(supabaseAuthService.getGoogleSignInUrl(anyString())).thenReturn(Mono.just(mockResponse));

        // Test the endpoint
        webClient
            .get()
            .uri("/api/oauth/google")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.url").isEqualTo("https://accounts.google.com/o/oauth2/auth?some-params");
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testHandleOAuthCallback() {
        // Prepare mock response
        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setAccessToken("mock-access-token");

        when(supabaseAuthService.exchangeCodeForSession(anyString())).thenReturn(Mono.just(mockResponse));

        // Test the endpoint
        webClient
            .get()
            .uri("/api/oauth/callback?code=test-code")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.access_token").isEqualTo("mock-access-token");
    }
}