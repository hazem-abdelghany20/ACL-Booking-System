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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    @Test
    void signUpUserToEvent_success() {
        Event event = new Event();
        event.setId(1L);
        event.setCapacity(2);
        event.setParticipantIds(new java.util.HashSet<>());
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event updated = eventService.signUpUserToEvent(1L, 100L);
        assertEquals(1, updated.getParticipantIds().size());
        assertEquals(true, updated.getParticipantIds().contains(100L));
    }

    @Test
    void signUpUserToEvent_alreadySignedUp() {
        Event event = new Event();
        event.setId(1L);
        event.setCapacity(2);
        java.util.Set<Long> participants = new java.util.HashSet<>();
        participants.add(100L);
        event.setParticipantIds(participants);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            eventService.signUpUserToEvent(1L, 100L);
        });
    }

    @Test
    void signUpUserToEvent_eventFull() {
        Event event = new Event();
        event.setId(1L);
        event.setCapacity(1);
        java.util.Set<Long> participants = new java.util.HashSet<>();
        participants.add(100L);
        event.setParticipantIds(participants);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            eventService.signUpUserToEvent(1L, 101L);
        });
    }




} 