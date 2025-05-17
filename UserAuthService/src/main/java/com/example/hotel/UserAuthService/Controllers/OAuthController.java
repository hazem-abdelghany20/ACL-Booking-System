package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.auth.service.AuthService;
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
    
    private final AuthService authService;
    
    @Value("${app.oauth.redirect-url:http://localhost:8081/api/oauth/callback}")
    private String redirectUrl;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    public OAuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @RequestMapping(value = "/google", method = {RequestMethod.GET, RequestMethod.POST})
    public Mono<ResponseEntity<Map>> getGoogleSignInUrl() {
        logger.info("Initiating Google sign-in");
        return authService.getGoogleSignInUrl()
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/callback")
    public Mono<RedirectView> handleOAuthCallback(@RequestParam("code") String code) {
        logger.info("Received OAuth callback with code");
        return authService.authenticateWithGoogle(code)
                .map(response -> {
                    logger.info("Successfully authenticated with Supabase via Google");
                    // Redirect to frontend with token
                    String redirectTo = frontendUrl + "/auth-success?token=" + response.getAccessToken() + 
                                       "&refresh_token=" + response.getRefreshToken() + 
                                       "&expires_in=" + response.getExpiresIn();
                    return new RedirectView(redirectTo);
                })
                .defaultIfEmpty(new RedirectView(frontendUrl + "/auth-error?message=Authentication%20failed"));
    }
    
    /**
     * Helper method to generate a redirect to Google sign-in
     * Used for direct browser access
     */
    @RequestMapping(value = "/google/redirect", method = {RequestMethod.GET, RequestMethod.POST})
    public Mono<RedirectView> redirectToGoogleSignIn() {
        logger.info("Redirecting to Google sign-in");
        return authService.getGoogleSignInUrl()
                .map(response -> {
                    String url = (String) response.get("url");
                    logger.info("Redirecting to Google sign-in: {}", url);
                    return new RedirectView(url);
                });
    }
} 