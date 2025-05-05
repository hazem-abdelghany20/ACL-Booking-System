package com.example.hotel.UserAuthService.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpVerificationRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Phone number must be in international format (e.g., +1234567890)")
    private String phone;
    
    @NotBlank(message = "OTP is required")
    private String token;
    
    // Getters and setters
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
} 