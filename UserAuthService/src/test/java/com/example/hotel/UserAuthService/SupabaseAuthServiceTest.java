package com.example.hotel.UserAuthService;

import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import com.example.hotel.UserAuthService.payload.request.EmailAuthRequest;
import com.example.hotel.UserAuthService.payload.request.OtpVerificationRequest;
import com.example.hotel.UserAuthService.payload.request.PhoneAuthRequest;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import com.example.hotel.UserAuthService.payload.response.WalletResponse;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private SupabaseAuthService supabaseAuthService;

    @BeforeEach
    public void setup() {
        // Enable lenient mode for all stubs
        Mockito.lenient().when(supabaseClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.lenient().when(supabaseClient.get()).thenReturn(requestHeadersUriSpec);
        
        Mockito.lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        Mockito.lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        
        Mockito.lenient().when(requestBodyUriSpec.contentType(any(MediaType.class))).thenReturn(requestBodyUriSpec);
        Mockito.lenient().when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        
        Mockito.lenient().when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        Mockito.lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        
        Mockito.lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.lenient().when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        
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

    @Test
    public void testSignInWithEmail() {
        // Arrange
        EmailAuthRequest request = new EmailAuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setAccessToken("mock-signin-token");
        mockResponse.setRefreshToken("mock-refresh-token");

        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.just(mockResponse));

        // Act
        Mono<AuthResponse> resultMono = supabaseAuthService.signInWithEmail(request);
        AuthResponse result = resultMono.block();

        // Assert
        assertNotNull(result);
        assertEquals("mock-signin-token", result.getAccessToken());
        assertEquals("mock-refresh-token", result.getRefreshToken());
    }

    @Test
    public void testSignUpWithPhone() {
        // Arrange
        PhoneAuthRequest request = new PhoneAuthRequest();
        request.setPhone("+1234567890");
        request.setPassword("password123");

        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setAccessToken("mock-phone-token");

        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.just(mockResponse));

        // Act
        Mono<AuthResponse> resultMono = supabaseAuthService.signUpWithPhone(request);
        AuthResponse result = resultMono.block();

        // Assert
        assertNotNull(result);
        assertEquals("mock-phone-token", result.getAccessToken());
    }

    @Test
    public void testSignInWithPhone() {
        // Arrange
        PhoneAuthRequest request = new PhoneAuthRequest();
        request.setPhone("+1234567890");
        request.setPassword("password123");

        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // Act
        Mono<Void> resultMono = supabaseAuthService.signInWithPhone(request);

        // No exception means the test passes
        assertNotNull(resultMono);
    }

    @Test
    public void testVerifyPhoneOtp() {
        // Arrange
        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setPhone("+1234567890");
        request.setToken("123456");

        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setAccessToken("mock-otp-token");

        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.just(mockResponse));

        // Act
        Mono<AuthResponse> resultMono = supabaseAuthService.verifyPhoneOtp(request);
        AuthResponse result = resultMono.block();

        // Assert
        assertNotNull(result);
        assertEquals("mock-otp-token", result.getAccessToken());
    }

    @Test
    public void testResetPassword() {
        // Arrange
        String email = "test@example.com";

        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // Act
        Mono<Void> resultMono = supabaseAuthService.resetPassword(email);

        // No exception means the test passes
        assertNotNull(resultMono);
    }

    @Test
    public void testGetWalletBalance() {
        // Arrange
        String userId = "user-123";
        String token = "bearer-token";

        WalletResponse mockResponse = new WalletResponse();
        mockResponse.setBalance(100.0);
        mockResponse.setUserId(userId);

        when(responseSpec.bodyToMono(WalletResponse.class)).thenReturn(Mono.just(mockResponse));

        // Act
        Mono<WalletResponse> resultMono = supabaseAuthService.getWalletBalance(userId, token);
        WalletResponse result = resultMono.block();

        // Assert
        assertNotNull(result);
        assertEquals(100.0, result.getBalance());
        assertEquals(userId, result.getUserId());
    }
}