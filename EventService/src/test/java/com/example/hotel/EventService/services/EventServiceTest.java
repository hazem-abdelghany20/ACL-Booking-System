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

    // New test for getEventsByParticipant(Long userId) - non-paginated version
    @Test
    void getEventsByParticipant_nonPaginated() {
        Long userId = 3L; // User 3 is participating in events 1 and 2
        List<Event> expectedEvents = Arrays.asList(testEvents.get(0), testEvents.get(1));

        when(eventRepository.findByParticipantId(userId)).thenReturn(expectedEvents);

        List<Event> result = eventService.getEventsByParticipant(userId);

        assertEquals(2, result.size());
        assertEquals(expectedEvents, result);
        verify(eventRepository, times(1)).findByParticipantId(userId);
    }

    // New test for getEventsByParticipant(Long userId, Pageable pageable) - paginated version
    @Test
    void getEventsByParticipant_paginated() {
        Long userId = 3L; // User 3 is participating in events 1 and 2
        List<Event> expectedEvents = Arrays.asList(testEvents.get(0), testEvents.get(1));
        Page<Event> eventsPage = new PageImpl<>(expectedEvents);
        Pageable pageable = PageRequest.of(0, 10);

        when(eventRepository.findByParticipantId(eq(userId), any(Pageable.class))).thenReturn(eventsPage);

        Page<Event> result = eventService.getEventsByParticipant(userId, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(expectedEvents, result.getContent());
        verify(eventRepository, times(1)).findByParticipantId(eq(userId), any(Pageable.class));
    }

    // New test for getAllUserEvents(Long userId) - non-paginated version
    @Test
    void getAllUserEvents_nonPaginated() {
        Long userId = 1L; // User 1 organizes events 1 and 3, participates in event 2
        // So we expect all 3 events in this case

        when(eventRepository.findAllEventsByUserId(userId)).thenReturn(testEvents);

        List<Event> result = eventService.getAllUserEvents(userId);

        assertEquals(3, result.size());
        assertEquals(testEvents, result);
        verify(eventRepository, times(1)).findAllEventsByUserId(userId);
    }

    // New test for getAllUserEvents(Long userId, Pageable pageable) - paginated version
    @Test
    void getAllUserEvents_paginated() {
        Long userId = 1L; // User 1 organizes events 1 and 3, participates in event 2
        Page<Event> eventsPage = new PageImpl<>(testEvents);
        Pageable pageable = PageRequest.of(0, 10);

        when(eventRepository.findAllEventsByUserId(eq(userId), any(Pageable.class))).thenReturn(eventsPage);

        Page<Event> result = eventService.getAllUserEvents(userId, pageable);

        assertEquals(3, result.getTotalElements());
        assertEquals(testEvents, result.getContent());
        verify(eventRepository, times(1)).findAllEventsByUserId(eq(userId), any(Pageable.class));
    }

    // New test for getEventsWithAvailableTickets(Pageable pageable)
    @Test
    void getEventsWithAvailableTickets() {
        List<Event> availableEvents = Arrays.asList(testEvents.get(0), testEvents.get(2)); // Events 1 and 3
        Page<Event> eventsPage = new PageImpl<>(availableEvents);
        Pageable pageable = PageRequest.of(0, 10);

        when(eventRepository.findEventsWithAvailableTickets(any(Pageable.class))).thenReturn(eventsPage);

        Page<Event> result = eventService.getEventsWithAvailableTickets(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(availableEvents, result.getContent());
        verify(eventRepository, times(1)).findEventsWithAvailableTickets(any(Pageable.class));
    }

    // New test for getAvailableTicketsCount(Long eventId) - when event exists
    @Test
    void getAvailableTicketsCount_eventExists() {
        Long eventId = 1L;
        Event event = testEvents.get(0); // This event has capacity 100 and 2 participants

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        int result = eventService.getAvailableTicketsCount(eventId);

        assertEquals(98, result); // 100 capacity - 2 participants = 98 available tickets
        verify(eventRepository, times(1)).findById(eventId);
    }

    // New test for getAvailableTicketsCount(Long eventId) - when event doesn't exist
    @Test
    void getAvailableTicketsCount_eventDoesNotExist() {
        Long eventId = 999L; // Non-existent event

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());
        // Assuming getEventById throws an exception when event is not found
        when(eventService.getEventById(eventId)).thenThrow(new RuntimeException("Event not found"));

        assertThrows(RuntimeException.class, () -> {
            eventService.getAvailableTicketsCount(eventId);
        });

        verify(eventRepository, times(1)).findById(eventId);
    }

    // Test for edge case: empty participant list
    @Test
    void getAvailableTicketsCount_emptyParticipantList() {
        Long eventId = 4L;
        Event event = new Event();
        event.setId(eventId);
        event.setCapacity(50);
        event.setParticipantIds(new HashSet<>()); // Empty participant list

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        int result = eventService.getAvailableTicketsCount(eventId);

        assertEquals(50, result); // All tickets available
        verify(eventRepository, times(1)).findById(eventId);
    }

    // Test for edge case: null participant list
    @Test
    void getAvailableTicketsCount_nullParticipantList() {
        Long eventId = 5L;
        Event event = new Event();
        event.setId(eventId);
        event.setCapacity(75);
        event.setParticipantIds(null); // Null participant list

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // This should throw a NullPointerException because we're trying to get the size of a null list
        assertThrows(NullPointerException.class, () -> {
            eventService.getAvailableTicketsCount(eventId);
        });

        verify(eventRepository, times(1)).findById(eventId);
    }

    // Test for edge case: no available tickets (fully booked)
    @Test
    void getAvailableTicketsCount_fullyBooked() {
        Long eventId = 6L;
        Event event = new Event();
        event.setId(eventId);
        event.setCapacity(3);

        Set<Long> participants = new HashSet<>();
        participants.add(1L);
        participants.add(2L);
        participants.add(3L);
        event.setParticipantIds(participants); // 3 participants for 3 capacity

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        int result = eventService.getAvailableTicketsCount(eventId);

        assertEquals(0, result); // No tickets available
        verify(eventRepository, times(1)).findById(eventId);
    }

    // Test for edge case: getEventsByParticipant with non-existent user
    @Test
    void getEventsByParticipant_nonExistentUser() {
        Long userId = 999L; // Non-existent user

        when(eventRepository.findByParticipantId(userId)).thenReturn(new ArrayList<>());

        List<Event> result = eventService.getEventsByParticipant(userId);

        assertTrue(result.isEmpty());
        verify(eventRepository, times(1)).findByParticipantId(userId);
    }

    // Test for edge case: getAllUserEvents with non-existent user
    @Test
    void getAllUserEvents_nonExistentUser() {
        Long userId = 999L; // Non-existent user

        when(eventRepository.findAllEventsByUserId(userId)).thenReturn(new ArrayList<>());

        List<Event> result = eventService.getAllUserEvents(userId);

        assertTrue(result.isEmpty());
        verify(eventRepository, times(1)).findAllEventsByUserId(userId);
    }

    // Test for edge case: getEventsWithAvailableTickets returning empty results
    @Test
    void getEventsWithAvailableTickets_noAvailableEvents() {
        Page<Event> emptyPage = new PageImpl<>(new ArrayList<>());
        Pageable pageable = PageRequest.of(0, 10);

        when(eventRepository.findEventsWithAvailableTickets(any(Pageable.class))).thenReturn(emptyPage);

        Page<Event> result = eventService.getEventsWithAvailableTickets(pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(eventRepository, times(1)).findEventsWithAvailableTickets(any(Pageable.class));
    }




}