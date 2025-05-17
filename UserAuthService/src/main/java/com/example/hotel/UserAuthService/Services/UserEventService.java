package com.example.hotel.UserAuthService.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Placeholder service that uses in-memory storage instead of a database.
 * This will be replaced with proper Supabase integration in the future.
 */
@Service
public class UserEventService {

    // In-memory map of user IDs to their event IDs
    private static final Map<Long, Set<Long>> userEvents = new HashMap<>();
    
    // Initialize with some test data
    static {
        Set<Long> user1Events = new HashSet<>();
        user1Events.add(101L);
        user1Events.add(102L);
        userEvents.put(1L, user1Events);
        
        Set<Long> user2Events = new HashSet<>();
        user2Events.add(103L);
        userEvents.put(2L, user2Events);
    }

    @Transactional
    public boolean addEventToUser(Long userId, Long eventId) {
        // Get or create the user's event set
        Set<Long> events = userEvents.getOrDefault(userId, new HashSet<>());
        
        // Check if user exists (for consistent error messaging with original implementation)
        if (!userEvents.containsKey(userId) && events.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        // Add the event and update the map
        events.add(eventId);
        userEvents.put(userId, events);
        
        return true;
    }

    @Transactional
    public boolean removeEventFromUser(Long userId, Long eventId) {
        // Check if user exists
        if (!userEvents.containsKey(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        Set<Long> events = userEvents.get(userId);
        
        // Check if user is registered for the event
        if (events == null || !events.contains(eventId)) {
            throw new RuntimeException("User is not registered for this event");
        }
        
        // Remove the event
        events.remove(eventId);
        
        return true;
    }
}