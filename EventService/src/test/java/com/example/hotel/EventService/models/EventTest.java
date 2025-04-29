package com.example.hotel.EventService.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventTest {

    @Test
    void simpleTest() {
        // A simple test that always passes
        String expected = "hello";
        String actual = "hello";
        assertEquals(expected, actual, "Simple test to verify test setup");
    }
    
    @Test
    void testBuilderPattern() {
        // Test the builder pattern
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = new Event.Builder()
                .title("Test Event")
                .description("Test Description")
                .startDateTime(startTime)
                .endDateTime(endTime)
                .location("Test Location")
                .capacity(100)
                .organizerId(1L)
                .eventType(EventType.PUBLIC)
                .build();
        
        assertNotNull(event);
        assertEquals("Test Event", event.getTitle());
        assertEquals("Test Description", event.getDescription());
        assertEquals(startTime, event.getStartDateTime());
        assertEquals(endTime, event.getEndDateTime());
        assertEquals("Test Location", event.getLocation());
        assertEquals(100, event.getCapacity());
        assertEquals(1L, event.getOrganizerId());
        assertEquals(EventType.PUBLIC, event.getEventType());
    }
} 