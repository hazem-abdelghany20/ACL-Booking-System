package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import com.example.hotel.UserAuthService.payload.request.EmailAuthRequest;
import com.example.hotel.UserAuthService.payload.request.LoginRequest;
import com.example.hotel.UserAuthService.payload.request.OtpVerificationRequest;
import com.example.hotel.UserAuthService.payload.request.PhoneAuthRequest;
import com.example.hotel.UserAuthService.payload.request.SignupRequest;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import com.example.hotel.UserAuthService.payload.response.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    
    @Mock
    private SupabaseAuthService supabaseAuthService;
    
    @InjectMocks
    private AuthController authController;
    
    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private EmailAuthRequest emailAuthRequest;
    private PhoneAuthRequest phoneAuthRequest;
    private OtpVerificationRequest otpRequest;
    private AuthResponse authResponse;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        loginRequest = new LoginRequest();
        loginRequest.setUsername("user@example.com");
        loginRequest.setPassword("password123");
        
        signupRequest = new SignupRequest();
        signupRequest.setEmail("user@example.com");
        signupRequest.setPassword("password123");
        
        emailAuthRequest = new EmailAuthRequest();
        emailAuthRequest.setEmail("user@example.com");
        emailAuthRequest.setPassword("password123");
        
        phoneAuthRequest = new PhoneAuthRequest();
        phoneAuthRequest.setPhone("+1234567890");
        phoneAuthRequest.setPassword("password123");
        
        otpRequest = new OtpVerificationRequest();
        otpRequest.setPhone("+1234567890");
        otpRequest.setToken("123456");
        
        authResponse = new AuthResponse();
        authResponse.setAccessToken("test-token");
        authResponse.setRefreshToken("refresh-token");
    }
    
    @Test
    void testAuthenticateUser() {
        // Arrange
        when(supabaseAuthService.signInWithEmail(any(EmailAuthRequest.class)))
            .thenReturn(Mono.just(authResponse));
        
        // Act
        Mono<ResponseEntity<AuthResponse>> resultMono = authController.authenticateUser(loginRequest);
        
        // Assert
        StepVerifier.create(resultMono)
            .expectNextMatches(response -> 
                response.getStatusCode() == HttpStatus.OK && 
                response.getBody() != null && 
                response.getBody().getAccessToken().equals("test-token")
            )
            .verifyComplete();
    }
    
    @Test
    void testRegisterUser() {
        // Arrange
        when(supabaseAuthService.signUpWithEmail(any(EmailAuthRequest.class)))
            .thenReturn(Mono.just(authResponse));
        
        // Act
        Mono<ResponseEntity<AuthResponse>> resultMono = authController.registerUser(signupRequest);
        
        // Assert
        StepVerifier.create(resultMono)
            .expectNextMatches(response -> 
                response.getStatusCode() == HttpStatus.OK && 
                response.getBody() != null && 
                response.getBody().getAccessToken().equals("test-token")
            )
            .verifyComplete();
    }
    
    @Test
    void testLogoutUser() {
        // Act
        ResponseEntity<?> response = authController.logoutUser();
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("You've been signed out!", ((MessageResponse)response.getBody()).getMessage());
    }
    
    @Test
    void testResetPassword() {
        // Arrange
        String email = "user@example.com";
        when(supabaseAuthService.resetPassword(anyString()))
            .thenReturn(Mono.empty());
        
        // Act
        ResponseEntity<?> response = authController.resetPassword(email);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    void testSignUpWithEmail() {
        // Arrange
        when(supabaseAuthService.signUpWithEmail(any(EmailAuthRequest.class)))
            .thenReturn(Mono.just(authResponse));
        
        // Act
        Mono<ResponseEntity<AuthResponse>> resultMono = authController.signUpWithEmail(emailAuthRequest);
        
        // Assert
        StepVerifier.create(resultMono)
            .expectNextMatches(response -> 
                response.getStatusCode() == HttpStatus.OK && 
                response.getBody() != null && 
                response.getBody().getAccessToken().equals("test-token")
            )
            .verifyComplete();
    }
    
    @Test
    void testSignInWithEmail() {
        // Arrange
        when(supabaseAuthService.signInWithEmail(any(EmailAuthRequest.class)))
            .thenReturn(Mono.just(authResponse));
        
        // Act
        Mono<ResponseEntity<AuthResponse>> resultMono = authController.signInWithEmail(emailAuthRequest);
        
        // Assert
        StepVerifier.create(resultMono)
            .expectNextMatches(response -> 
                response.getStatusCode() == HttpStatus.OK && 
                response.getBody() != null && 
                response.getBody().getAccessToken().equals("test-token")
            )
            .verifyComplete();
    }
    
    @Test
    void testSignUpWithPhone() {
        // Arrange
        when(supabaseAuthService.signUpWithPhone(any(PhoneAuthRequest.class)))
            .thenReturn(Mono.just(authResponse));
        
        // Act
        Mono<ResponseEntity<AuthResponse>> resultMono = authController.signUpWithPhone(phoneAuthRequest);
        
        // Assert
        StepVerifier.create(resultMono)
            .expectNextMatches(response -> 
                response.getStatusCode() == HttpStatus.OK && 
                response.getBody() != null && 
                response.getBody().getAccessToken().equals("test-token")
            )
            .verifyComplete();
    }
    
    @Test
    void testSignInWithPhone() {
        // Arrange
        when(supabaseAuthService.signInWithPhone(any(PhoneAuthRequest.class)))
            .thenReturn(Mono.empty());
        
        // Act
        Mono<ResponseEntity<Void>> resultMono = authController.signInWithPhone(phoneAuthRequest);
        
        // Assert
        StepVerifier.create(resultMono)
            .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
            .verifyComplete();
    }
    
    @Test
    void testVerifyPhoneOtp() {
        // Arrange
        when(supabaseAuthService.verifyPhoneOtp(any(OtpVerificationRequest.class)))
            .thenReturn(Mono.just(authResponse));
        
        // Act
        Mono<ResponseEntity<AuthResponse>> resultMono = authController.verifyPhoneOtp(otpRequest);
        
        // Assert
        StepVerifier.create(resultMono)
            .expectNextMatches(response -> 
                response.getStatusCode() == HttpStatus.OK && 
                response.getBody() != null && 
                response.getBody().getAccessToken().equals("test-token")
            )
            .verifyComplete();
    }
    
    @Test
    void testAuthenticateUser_Unauthorized() {
        // Arrange
        when(supabaseAuthService.signInWithEmail(any(EmailAuthRequest.class)))
            .thenReturn(Mono.empty());
        
        // Act
        Mono<ResponseEntity<AuthResponse>> resultMono = authController.authenticateUser(loginRequest);
        
        // Assert
        StepVerifier.create(resultMono)
            .expectNextMatches(response -> response.getStatusCode() == HttpStatus.UNAUTHORIZED)
            .verifyComplete();
    }
} 