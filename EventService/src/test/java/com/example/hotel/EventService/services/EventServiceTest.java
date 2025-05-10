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
import com.example.hotel.EventService.clients.PaymentClient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private EventService eventService;

    @Mock
    private PaymentClient paymentClient;
    
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

    @Test
    void processEventPayment_EventNotFound_ThrowsException() {

        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                eventService.processEventPayment(1L, 1L));

        verify(eventRepository).findById(1L);
        verifyNoInteractions(paymentClient);
    }

    @Test
    void processEventPayment_NegativePrice_ThrowsException() {

        Event event = new Event();
        event.setId(1L);
        event.setPrice(-10.0);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                eventService.processEventPayment(1L, 1L));

        assertEquals("Invalid event price: -10.0", exception.getMessage());
        verify(eventRepository).findById(1L);
        verifyNoInteractions(paymentClient);
    }

    @Test
    void processEventPayment_FreeEvent_ReturnsSuccessMessage() {

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Free Event");
        event.setPrice(0.0);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        String result = eventService.processEventPayment(1L, 1L);

        assertEquals("No payment required for free event", result);
        verify(eventRepository).findById(1L);
        verifyNoInteractions(paymentClient);
    }



    @Test
    void processEventPayment_InsufficientFunds_ReturnsFailureMessage() {

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Expensive Event");
        event.setPrice(100.0);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", false);
        responseBody.put("message", "Insufficient balance");

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        when(paymentClient.processPayment(1L, 100.0)).thenReturn(responseEntity);

        String result = eventService.processEventPayment(1L, 1L);

        assertEquals("Payment failed: Insufficient funds", result);
        verify(eventRepository).findById(1L);
        verify(paymentClient).processPayment(1L, 100.0);
    }



    @Test
    void processEventPayment_VerifiesBalanceChangesCorrectly() {

        double initialBalance = 100.0;
        double eventPrice = 25.0;
        double expectedRemainingBalance = initialBalance - eventPrice;

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setPrice(eventPrice);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("remainingBalance", expectedRemainingBalance);
        ResponseEntity<Map<String, Object>> response =
                new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(paymentClient.processPayment(eq(1L), eq(eventPrice))).thenReturn(response);

        String result = eventService.processEventPayment(1L, 1L);


        verify(paymentClient).processPayment(1L, eventPrice);


        String expectedMessage = "Payment processed successfully for event: Test Event. Your new balance is: " + expectedRemainingBalance;
        assertEquals(expectedMessage, result);

    }


} 