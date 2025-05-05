package com.example.hotel.UserAuthService;

import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import com.example.hotel.UserAuthService.payload.request.EmailAuthRequest;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupabaseAuthServiceTest {

    @Mock
    private WebClient supabaseClient;

    @Mock
    private WebClient supabaseAdminClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;
    
    private SupabaseAuthService supabaseAuthService;

    @BeforeEach
    public void setup() {
        when(supabaseClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any(MediaType.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        // Create the service with the mocked WebClient
        supabaseAuthService = new SupabaseAuthService(supabaseClient, supabaseAdminClient);
    }

    @Test
    public void testSignUpWithEmail() {
        // Arrange
        EmailAuthRequest request = new EmailAuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setAccessToken("mock-token");
        
        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.just(mockResponse));

        // Act
        Mono<AuthResponse> resultMono = supabaseAuthService.signUpWithEmail(request);
        AuthResponse result = resultMono.block();

        // Assert
        assertNotNull(result);
        assertEquals("mock-token", result.getAccessToken());
    }
} 