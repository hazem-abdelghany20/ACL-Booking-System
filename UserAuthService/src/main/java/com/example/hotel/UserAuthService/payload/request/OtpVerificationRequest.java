package com.example.hotel.UserAuthService.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpVerificationRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\+[0-9]+", message = "Phone number must start with '+' followed by digits")
    private String phone;

    @NotBlank(message = "OTP token is required")
    private String token;

    public OtpVerificationRequest() {
    }

    public OtpVerificationRequest(String phone, String token) {
        this.phone = phone;
        this.token = token;
    }

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