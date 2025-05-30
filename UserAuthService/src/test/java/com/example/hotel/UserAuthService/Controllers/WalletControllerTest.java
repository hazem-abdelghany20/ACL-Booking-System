package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.MockWalletService;
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
    private MockWalletService mockWalletService;  // Changed to MockWalletService

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
        WalletResponse mockWalletResponse = new WalletResponse();
        mockWalletResponse.setUserId(userId);
        mockWalletResponse.setBalance(100.0);

        when(mockWalletService.getWalletBalance(anyString(), anyString()))
                .thenReturn(Mono.just(mockWalletResponse));

        // Act
        Mono<ResponseEntity<Map<String, Object>>> resultMono = walletController.getBalance(userId, token);

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(response -> {
                    Map<String, Object> body = response.getBody();
                    return response.getStatusCode() == HttpStatus.OK &&
                            body != null &&
                            body.get("userId").equals(userId) &&
                            (Double) body.get("balance") == 100.0;
                })
                .verifyComplete();
    }

    @Test
    public void testAddFunds() {
        // Arrange
        WalletResponse mockWalletResponse = new WalletResponse();
        mockWalletResponse.setUserId(userId);
        mockWalletResponse.setBalance(150.0); // 100 + 50

        when(mockWalletService.addFunds(anyString(), anyDouble(), anyString()))
                .thenReturn(Mono.just(mockWalletResponse));

        // Act
        Mono<ResponseEntity<Map<String, Object>>> resultMono = walletController.addFunds(userId, 50.0, token);

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(response -> {
                    Map<String, Object> body = response.getBody();
                    return response.getStatusCode() == HttpStatus.OK &&
                            body != null &&
                            body.get("userId").equals(userId) &&
                            (Double) body.get("balance") == 150.0;
                })
                .verifyComplete();
    }

    @Test
    public void testDeductFunds() {
        // Arrange
        WalletResponse mockResponse = new WalletResponse();
        mockResponse.setUserId(userId);
        mockResponse.setBalance(50.0); // 100 - 50

        when(mockWalletService.deductFunds(anyString(), anyDouble(), anyString()))
                .thenReturn(Mono.just(mockResponse));

        // Act
        Mono<ResponseEntity<Map<String, Object>>> resultMono = walletController.deductFunds(userId, 50.0, token);

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(response -> {
                    Map<String, Object> body = response.getBody();
                    return response.getStatusCode() == HttpStatus.OK &&
                            body != null &&
                            body.get("userId").equals(userId) &&
                            (Double) body.get("balance") == 50.0;
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

        when(mockWalletService.getTransactionHistory(anyString(), anyString()))
                .thenReturn(Mono.just(mockResponse));

        // Act
        Mono<ResponseEntity<Map<String, Object>>> resultMono = walletController.getTransactionHistory(userId, token);

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
        when(mockWalletService.getWalletBalance(anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        Mono<ResponseEntity<Map<String, Object>>> resultMono = walletController.getBalance(userId, token);

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }
}