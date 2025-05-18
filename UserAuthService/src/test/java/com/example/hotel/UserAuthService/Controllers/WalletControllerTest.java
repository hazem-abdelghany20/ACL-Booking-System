package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import com.example.hotel.UserAuthService.payload.response.WalletResponse;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletControllerTest {

    @Mock
    private SupabaseAuthService supabaseAuthService;

    @InjectMocks
    private WalletController walletController;

    private final String userId = "user-123";
    private final String token = "Bearer test-token";

    @BeforeEach
    public void setup() {
        // No additional setup needed - annotations handle mock creation
    }

    @Test
    public void testGetWalletBalance() {
        // Arrange
        WalletResponse mockResponse = new WalletResponse();
        mockResponse.setUserId(userId);
        mockResponse.setBalance(100.0);

        when(supabaseAuthService.getWalletBalance(anyString(), anyString()))
                .thenReturn(Mono.just(mockResponse));

        // Act
        Mono<ResponseEntity<WalletResponse>> resultMono = walletController.getWalletBalance(userId, token);

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(response -> {
                    WalletResponse body = response.getBody();
                    return response.getStatusCode() == HttpStatus.OK &&
                            body != null &&
                            body.getUserId().equals(userId) &&
                            body.getBalance() == 100.0;
                })
                .verifyComplete();
    }

    @Test
    public void testAddFunds() {
        // Arrange
        WalletResponse mockResponse = new WalletResponse();
        mockResponse.setUserId(userId);
        mockResponse.setBalance(150.0); // 100 + 50

        when(supabaseAuthService.addFunds(anyString(), anyDouble(), anyString()))
                .thenReturn(Mono.just(mockResponse));

        // Act
        Mono<ResponseEntity<WalletResponse>> resultMono = walletController.addFunds(userId, 50.0, token);

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(response -> {
                    WalletResponse body = response.getBody();
                    return response.getStatusCode() == HttpStatus.OK &&
                            body != null &&
                            body.getUserId().equals(userId) &&
                            body.getBalance() == 150.0;
                })
                .verifyComplete();
    }

    @Test
    public void testDeductFunds() {
        // Arrange
        WalletResponse mockResponse = new WalletResponse();
        mockResponse.setUserId(userId);
        mockResponse.setBalance(50.0); // 100 - 50

        when(supabaseAuthService.deductFunds(anyString(), anyDouble(), anyString()))
                .thenReturn(Mono.just(mockResponse));

        // Act
        Mono<ResponseEntity<WalletResponse>> resultMono = walletController.deductFunds(userId, 50.0, token);

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(response -> {
                    WalletResponse body = response.getBody();
                    return response.getStatusCode() == HttpStatus.OK &&
                            body != null &&
                            body.getUserId().equals(userId) &&
                            body.getBalance() == 50.0;
                })
                .verifyComplete();
    }

    @Test
    public void testGetTransactionHistory() {
        // Arrange
        Map<String, Object> mockTransaction1 = new HashMap<>();
        mockTransaction1.put("id", "txn-1");
        mockTransaction1.put("amount", 50.0);
        mockTransaction1.put("type", "deposit");

        Map<String, Object> mockTransaction2 = new HashMap<>();
        mockTransaction2.put("id", "txn-2");
        mockTransaction2.put("amount", 25.0);
        mockTransaction2.put("type", "withdrawal");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("transactions", List.of(mockTransaction1, mockTransaction2));

        when(supabaseAuthService.getTransactionHistory(anyString(), anyString()))
                .thenReturn(Mono.just(mockResponse));

        // Act
        Mono<ResponseEntity<Map>> resultMono = walletController.getTransactionHistory(userId, token);

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.getStatusCode() == HttpStatus.OK &&
                                response.getBody() != null &&
                                response.getBody().containsKey("transactions")
                )
                .verifyComplete();
    }

    @Test
    public void testGetWalletBalance_NotFound() {
        // Arrange
        when(supabaseAuthService.getWalletBalance(anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        Mono<ResponseEntity<WalletResponse>> resultMono = walletController.getWalletBalance(userId, token);

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }
}