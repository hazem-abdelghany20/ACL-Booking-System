package com.example.hotel.UserAuthService.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    
    @GetMapping("/google-login")
    public String googleLoginPage() {
        return "google-login";
    }
} 