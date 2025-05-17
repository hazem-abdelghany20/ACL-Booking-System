package com.example.hotel.UserAuthService.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    
    @GetMapping("/google-login")
    public String googleLoginPage() {
        return "google-login";
    }
    
    @GetMapping("/auth-success")
    public String authSuccessPage() {
        return "auth-success";
    }
    
    @GetMapping("/auth-error")
    public String authErrorPage() {
        return "auth-error";
    }
} 