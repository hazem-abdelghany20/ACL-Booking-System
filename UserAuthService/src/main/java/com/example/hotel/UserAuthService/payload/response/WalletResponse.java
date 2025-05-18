package com.example.hotel.UserAuthService.payload.response;

import com.example.hotel.UserAuthService.models.Wallet;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletResponse {
    private String id;

    @JsonProperty("user_id")
    private String userId;

    private Double balance;
    private String message;
    private Boolean success;

    // Default constructor
    public WalletResponse() {
        this.success = true;
    }

    // Constructor from Wallet model
    public WalletResponse(Wallet wallet) {
        this.id = wallet.getId();
        this.userId = wallet.getUserId();
        this.balance = wallet.getBalance();
        this.success = true;
    }

    // Constructor for successful creation or retrieval
    public WalletResponse(String userId, Double balance, boolean success) {
        this.userId = userId;
        this.balance = balance;
        this.success = success;
    }

    // Constructor for success/failure with message
    public WalletResponse(String userId, Double balance, boolean success, String message) {
        this.userId = userId;
        this.balance = balance;
        this.success = success;
        this.message = message;
    }

    // Constructor for error scenarios
    public WalletResponse(String message, String userId, Double balance) {
        this.message = message;
        this.userId = userId;
        this.balance = balance;
        this.success = false;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "WalletResponse{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", balance=" + balance +
                ", message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}