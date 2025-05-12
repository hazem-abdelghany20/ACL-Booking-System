package com.example.hotel.UserAuthService.Controllers;

import com.example.hotel.UserAuthService.Services.UserEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserEventController {

    @Autowired
    private UserEventService userEventService;

    @PostMapping("/{userId}/events/{eventId}")
    public ResponseEntity<?> addEventToUser(@PathVariable Long userId, @PathVariable Long eventId) {
        try {
            boolean success = userEventService.addEventToUser(userId, eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event added to user successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{userId}/events/{eventId}")
    public ResponseEntity<?> removeEventFromUser(@PathVariable Long userId, @PathVariable Long eventId) {
        try {
            boolean success = userEventService.removeEventFromUser(userId, eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event removed from user successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


}