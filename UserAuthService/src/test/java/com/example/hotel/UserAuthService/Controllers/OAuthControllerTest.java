package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.auth.service.AuthService;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private AuthService authService;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        
        // Reset mocks before each test
        reset(authService);
    }

    @Test
    @WithMockUser
    public void testGetGoogleSignInUrl() throws Exception {
        // Prepare mock response
        Map<String, String> mockResponse = new HashMap<>();
        mockResponse.put("url", "https://accounts.google.com/o/oauth2/auth?some-params");

        // Configure the mock
        when(authService.getGoogleSignInUrl())
            .thenReturn(Mono.just(mockResponse));

        // Test the endpoint
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/oauth/google")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        
        // Verify the mock was called
        verify(authService, times(1)).getGoogleSignInUrl();
    }

    @Test
    @WithMockUser
    public void testHandleOAuthCallback() throws Exception {
        // Prepare mock response
        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setAccessToken("mock-access-token");
        mockResponse.setRefreshToken("mock-refresh-token");
        
        // Set the user data with an ID
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", "user-123");
        mockResponse.setUser(userData);

        // Configure the mock
        when(authService.authenticateWithGoogle(anyString()))
            .thenReturn(Mono.just(mockResponse));

        // Test the endpoint
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/oauth/callback")
                .param("code", "test-code")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        
        // Verify the mock was called
        verify(authService, times(1)).authenticateWithGoogle(anyString());
    }
}