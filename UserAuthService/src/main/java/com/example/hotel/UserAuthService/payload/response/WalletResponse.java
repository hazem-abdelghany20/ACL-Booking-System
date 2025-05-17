package com.example.hotel.UserAuthService.payload.response;

/**
 * Response class for wallet operations
 */
public class WalletResponse {
    private String userId;
    private Double balance;
    private String currency;
    private String lastTransaction;
    
    public WalletResponse() {
    }
    
    public WalletResponse(String userId, Double balance) {
        this.userId = userId;
        this.balance = balance;
        this.currency = "USD"; // Default currency
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLastTransaction() {
        return lastTransaction;
    }

    public void setLastTransaction(String lastTransaction) {
        this.lastTransaction = lastTransaction;
    }
} 