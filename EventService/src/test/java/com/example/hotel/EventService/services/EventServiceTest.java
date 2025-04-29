package com.example.hotel.EventService.services;

import com.example.hotel.EventService.models.Event;
import com.example.hotel.EventService.repositories.CategoryRepository;
import com.example.hotel.EventService.repositories.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private EventService eventService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void simpleTest() {
        // A simple test that always passes
        String expected = "hello";
        String actual = "hello";
        assertEquals(expected, actual, "Simple test to verify test setup");
    }
    
    @Test
    void getAllEventsSimpleTest() {
        // Mock data
        List<Event> mockEvents = new ArrayList<>();
        when(eventRepository.findAll()).thenReturn(mockEvents);
        
        // Test
        List<Event> result = eventService.getAllEvents();
        
        // Verify
        assertEquals(mockEvents, result);
    }
} 