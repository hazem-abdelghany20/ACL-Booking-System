package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.SupabaseAuthService;
import com.example.hotel.UserAuthService.payload.response.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OAuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthController.class);
    
    private final SupabaseAuthService supabaseAuthService;
    
    @Value("${app.oauth.redirect-url:http://localhost:8081/api/oauth/callback}")
    private String redirectUrl;
    
    public OAuthController(SupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }
    
    @RequestMapping(value = "/google", method = {RequestMethod.GET, RequestMethod.POST})
    public Mono<ResponseEntity<Map>> getGoogleSignInUrl() {
        logger.info("Initiating Google sign-in");
        return supabaseAuthService.getGoogleSignInUrl(redirectUrl)
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/callback")
    public Mono<ResponseEntity<AuthResponse>> handleOAuthCallback(@RequestParam("code") String code) {
        logger.info("Received OAuth callback with code");
        return supabaseAuthService.exchangeCodeForSession(code)
                .map(ResponseEntity::ok);
    }
    
    /**
     * Helper method to generate a redirect to Google sign-in
     * Used for direct browser access
     */
    @RequestMapping(value = "/google/redirect", method = {RequestMethod.GET, RequestMethod.POST})
    public Mono<RedirectView> redirectToGoogleSignIn() {
        logger.info("Redirecting to Google sign-in");
        return supabaseAuthService.getGoogleSignInUrl(redirectUrl)
                .map(response -> {
                    String url = (String) response.get("url");
                    logger.info("Redirecting to Google sign-in: {}", url);
                    return new RedirectView(url);
                });
    }
    
    @GetMapping("/providers")
    public Mono<ResponseEntity<Map>> getProviders() {
        logger.info("Getting OAuth providers");
        return supabaseAuthService.testGoogleOAuth()
                .map(ResponseEntity::ok);
    }
} 