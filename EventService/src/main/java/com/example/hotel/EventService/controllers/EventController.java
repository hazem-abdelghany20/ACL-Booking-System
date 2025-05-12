package com.example.hotel.EventService.controllers;

import com.example.hotel.EventService.models.Event;
import com.example.hotel.EventService.models.EventType;
import com.example.hotel.EventService.services.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/events")
public class EventController {
    
    @Autowired
    private EventService eventService;
    
    // Get all events with pagination
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDateTime") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Event> pageEvents = eventService.getEventsPaginated(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("events", pageEvents.getContent());
        response.put("currentPage", pageEvents.getNumber());
        response.put("totalItems", pageEvents.getTotalElements());
        response.put("totalPages", pageEvents.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    // Get event by id
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
    
    // Create new event
    @PostMapping
    public ResponseEntity<Event> createEvent(
            @Valid @RequestBody Event event,
            @RequestParam(required = false) List<String> categories) {
        Event createdEvent = eventService.createEvent(event, categories);
        return ResponseEntity.ok(createdEvent);
    }
    
    // Update event
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody Event eventDetails,
            @RequestParam(required = false) List<String> categories) {
        Event updatedEvent = eventService.updateEvent(id, eventDetails, categories);
        return ResponseEntity.ok(updatedEvent);
    }
    
    // Delete event
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
    
    // Get events by organizer
    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<Map<String, Object>> getEventsByOrganizer(
            @PathVariable Long organizerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> pageEvents = eventService.getEventsByOrganizer(organizerId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("events", pageEvents.getContent());
        response.put("currentPage", pageEvents.getNumber());
        response.put("totalItems", pageEvents.getTotalElements());
        response.put("totalPages", pageEvents.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    // Get public events
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> pageEvents = eventService.getPublicEvents(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("events", pageEvents.getContent());
        response.put("currentPage", pageEvents.getNumber());
        response.put("totalItems", pageEvents.getTotalElements());
        response.put("totalPages", pageEvents.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    // Get upcoming events
    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> pageEvents = eventService.getUpcomingEvents(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("events", pageEvents.getContent());
        response.put("currentPage", pageEvents.getNumber());
        response.put("totalItems", pageEvents.getTotalElements());
        response.put("totalPages", pageEvents.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    // Get events by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getEventsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> pageEvents = eventService.getEventsByCategory(categoryId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("events", pageEvents.getContent());
        response.put("currentPage", pageEvents.getNumber());
        response.put("totalItems", pageEvents.getTotalElements());
        response.put("totalPages", pageEvents.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    // Search events
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchEvents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> pageEvents = eventService.searchEvents(keyword, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("events", pageEvents.getContent());
        response.put("currentPage", pageEvents.getNumber());
        response.put("totalItems", pageEvents.getTotalElements());
        response.put("totalPages", pageEvents.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    // Event actions
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Event> publishEvent(@PathVariable Long id) {
        Event event = eventService.publishEvent(id);
        return ResponseEntity.ok(event);
    }
    
    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<Event> unpublishEvent(@PathVariable Long id) {
        Event event = eventService.unpublishEvent(id);
        return ResponseEntity.ok(event);
    }
    
    @PatchMapping("/{id}/type")
    public ResponseEntity<Event> changeEventType(
            @PathVariable Long id,
            @RequestParam EventType eventType) {
        Event event = eventService.changeEventType(id, eventType);
        return ResponseEntity.ok(event);
    }
    
    @PostMapping("/{id}/signup")
    public ResponseEntity<?> signUpUserToEvent(@PathVariable("id") Long eventId, @RequestParam Long userId) {
        try {
            Event updatedEvent = eventService.signUpUserToEvent(eventId, userId);
            return ResponseEntity.ok(updatedEvent);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // Get events a user is participating in
    @GetMapping("/participant/{userId}")
    public ResponseEntity<Map<String, Object>> getEventsByParticipant(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Event> pageEvents = eventService.getEventsByParticipant(userId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("events", pageEvents.getContent());
        response.put("currentPage", pageEvents.getNumber());
        response.put("totalItems", pageEvents.getTotalElements());
        response.put("totalPages", pageEvents.getTotalPages());

        return ResponseEntity.ok(response);
    }

    // Get all events related to a user (both as organizer and participant)
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getAllUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Event> pageEvents = eventService.getAllUserEvents(userId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("events", pageEvents.getContent());
        response.put("currentPage", pageEvents.getNumber());
        response.put("totalItems", pageEvents.getTotalElements());
        response.put("totalPages", pageEvents.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Get events with available tickets
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getEventsWithAvailableTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> pageEvents = eventService.getEventsWithAvailableTickets(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("events", pageEvents.getContent());
        response.put("currentPage", pageEvents.getNumber());
        response.put("totalItems", pageEvents.getTotalElements());
        response.put("totalPages", pageEvents.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get available tickets count for a specific event
     */
    @GetMapping("/{id}/available-tickets")
    public ResponseEntity<Map<String, Object>> getAvailableTicketsCount(@PathVariable Long id) {
        int availableTickets = eventService.getAvailableTicketsCount(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("eventId", id);
        response.put("availableTickets", availableTickets);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Book tickets for an event
     */
    @PostMapping("/{id}/book")
    public ResponseEntity<?> bookEventTickets(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int numberOfTickets) {
        try {
            Map<String, Object> booking = eventService.bookEventTickets(id, userId, numberOfTickets);
            return ResponseEntity.ok(booking);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

} 