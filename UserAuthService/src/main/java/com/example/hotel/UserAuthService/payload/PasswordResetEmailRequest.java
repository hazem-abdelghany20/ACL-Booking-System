package com.example.hotel.UserAuthService.payload;

// TODO: Add validation annotations if needed (e.g., @Email, @NotBlank)
public class PasswordResetEmailRequest {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
} 