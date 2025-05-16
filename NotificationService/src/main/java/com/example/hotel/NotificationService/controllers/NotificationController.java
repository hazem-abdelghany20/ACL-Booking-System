package com.example.hotel.NotificationService.controllers;

import com.example.hotel.NotificationService.dto.NotificationRequest;
import com.example.hotel.NotificationService.models.Notification;
import com.example.hotel.NotificationService.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(@RequestBody NotificationRequest request) {
        Notification saved = notificationService.createNotification(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getAllForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/command/{action}/{id}")
    public ResponseEntity<String> markNotification(
            @PathVariable String action,
            @PathVariable String id) {
        try {
            notificationService.executeCommand(action, id);
            return ResponseEntity.ok("Notification " + action + " successful.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
