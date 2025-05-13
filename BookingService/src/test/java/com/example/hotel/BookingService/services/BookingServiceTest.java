package com.example.hotel.BookingService.Services;

import com.example.hotel.BookingService.Clients.AvailabilityClient;
import com.example.hotel.BookingService.Clients.EventClient;
import com.example.hotel.BookingService.Clients.UserClient;
import com.example.hotel.BookingService.rabbitmq.BookingProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.util.HashMap;
import java.util.Map;

class BookingServiceTest {

    @Mock
    private AvailabilityClient availabilityClient;

    @Mock
    private EventClient eventClient;

    @Mock
    private UserClient userClient;

    @Mock
    private BookingProducer bookingProducer;

    @InjectMocks
    private BookingService bookingService;

    // Test data
    private final Long userId = 1L;
    private final Long eventId = 1L;
    private final Double eventPrice = 49.99;
    private final Double userBalance = 100.0;
    private final String eventTitle = "Test Event";
    private final String eventCurrency = "USD";

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
    void processEventPayment_Success() {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("price", eventPrice);
        eventData.put("title", eventTitle);
        eventData.put("currency", eventCurrency);
        ResponseEntity<Map<String, Object>> eventResponse = new ResponseEntity<>(eventData, HttpStatus.OK);


        Map<String, Object> registrationCheckData = new HashMap<>();
        registrationCheckData.put("success", true);
        registrationCheckData.put("isRegistered", false);
        ResponseEntity<Map<String, Object>> registrationResponse = new ResponseEntity<>(registrationCheckData, HttpStatus.OK);


        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("success", true);
        paymentData.put("balance", userBalance - eventPrice);
        ResponseEntity<Map<String, Object>> paymentResponse = new ResponseEntity<>(paymentData, HttpStatus.OK);


        Map<String, Object> balanceData = new HashMap<>();
        balanceData.put("success", true);
        balanceData.put("balance", userBalance - eventPrice);
        ResponseEntity<Map<String, Object>> balanceResponse = new ResponseEntity<>(balanceData, HttpStatus.OK);


        when(eventClient.getEvent(eq(eventId))).thenReturn((ResponseEntity) eventResponse);
        when(eventClient.isUserRegisteredForEvent(eq(eventId), eq(userId))).thenReturn((ResponseEntity) registrationResponse);
        when(userClient.processPayment(eq(userId), eq(eventPrice))).thenReturn((ResponseEntity) paymentResponse);
        when(userClient.getBalance(eq(userId))).thenReturn((ResponseEntity) balanceResponse);
        when(eventClient.addParticipantToEvent(eq(eventId), eq(userId))).thenReturn((ResponseEntity) new ResponseEntity<>(Map.of("success", true), HttpStatus.OK));
        when(userClient.addEventToUser(eq(userId), eq(eventId))).thenReturn((ResponseEntity) new ResponseEntity<>(Map.of("success", true), HttpStatus.OK));


        String result = bookingService.processEventPayment(userId, eventId);


        assertTrue(result.contains("Payment processed successfully"));
        assertTrue(result.contains(eventTitle));
        assertTrue(result.contains(String.valueOf(userBalance - eventPrice)));


        verify(eventClient).getEvent(eventId);
        verify(eventClient).isUserRegisteredForEvent(eventId, userId);
        verify(userClient).processPayment(userId, eventPrice);
        verify(userClient).getBalance(userId);
        verify(eventClient).addParticipantToEvent(eventId, userId);
        verify(userClient).addEventToUser(userId, eventId);
    }

    @Test
    void processEventPayment_AlreadyRegistered() {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("price", eventPrice);
        eventData.put("title", eventTitle);
        eventData.put("currency", eventCurrency);
        ResponseEntity<Map<String, Object>> eventResponse = new ResponseEntity<>(eventData, HttpStatus.OK);


        Map<String, Object> registrationCheckData = new HashMap<>();
        registrationCheckData.put("success", true);
        registrationCheckData.put("isRegistered", true);
        ResponseEntity<Map<String, Object>> registrationResponse = new ResponseEntity<>(registrationCheckData, HttpStatus.OK);


        when(eventClient.getEvent(eq(eventId))).thenReturn((ResponseEntity) eventResponse);
        when(eventClient.isUserRegisteredForEvent(eq(eventId), eq(userId))).thenReturn((ResponseEntity) registrationResponse);


        String result = bookingService.processEventPayment(userId, eventId);


        assertTrue(result.contains("already registered"));
        assertTrue(result.contains(eventTitle));


        verify(eventClient).getEvent(eventId);
        verify(eventClient).isUserRegisteredForEvent(eventId, userId);
        verify(userClient, never()).processPayment(any(), any());
        verify(eventClient, never()).addParticipantToEvent(any(), any());
        verify(userClient, never()).addEventToUser(any(), any());
    }

    @Test
    void processEventPayment_FreeEvent() {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("price", 0.0);
        eventData.put("title", eventTitle);
        eventData.put("currency", eventCurrency);
        ResponseEntity<Map<String, Object>> eventResponse = new ResponseEntity<>(eventData, HttpStatus.OK);


        Map<String, Object> registrationCheckData = new HashMap<>();
        registrationCheckData.put("success", true);
        registrationCheckData.put("isRegistered", false);
        ResponseEntity<Map<String, Object>> registrationResponse = new ResponseEntity<>(registrationCheckData, HttpStatus.OK);


        when(eventClient.getEvent(eq(eventId))).thenReturn((ResponseEntity) eventResponse);
        when(eventClient.isUserRegisteredForEvent(eq(eventId), eq(userId))).thenReturn((ResponseEntity) registrationResponse);
        when(eventClient.addParticipantToEvent(eq(eventId), eq(userId))).thenReturn((ResponseEntity) new ResponseEntity<>(Map.of("success", true), HttpStatus.OK));
        when(userClient.addEventToUser(eq(userId), eq(eventId))).thenReturn((ResponseEntity) new ResponseEntity<>(Map.of("success", true), HttpStatus.OK));


        String result = bookingService.processEventPayment(userId, eventId);


        assertTrue(result.contains("Successfully registered for free event"));
        assertTrue(result.contains(eventTitle));


        verify(eventClient).getEvent(eventId);
        verify(eventClient).isUserRegisteredForEvent(eventId, userId);
        verify(userClient, never()).processPayment(any(), any());
        verify(eventClient).addParticipantToEvent(eventId, userId);
        verify(userClient).addEventToUser(userId, eventId);
    }

    @Test
    void processEventPayment_PaymentFailed() {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("price", eventPrice);
        eventData.put("title", eventTitle);
        eventData.put("currency", eventCurrency);
        ResponseEntity<Map<String, Object>> eventResponse = new ResponseEntity<>(eventData, HttpStatus.OK);


        Map<String, Object> registrationCheckData = new HashMap<>();
        registrationCheckData.put("success", true);
        registrationCheckData.put("isRegistered", false);
        ResponseEntity<Map<String, Object>> registrationResponse = new ResponseEntity<>(registrationCheckData, HttpStatus.OK);


        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("success", false);
        paymentData.put("message", "Insufficient funds");
        ResponseEntity<Map<String, Object>> paymentResponse = new ResponseEntity<>(paymentData, HttpStatus.OK);


        when(eventClient.getEvent(eq(eventId))).thenReturn((ResponseEntity) eventResponse);
        when(eventClient.isUserRegisteredForEvent(eq(eventId), eq(userId))).thenReturn((ResponseEntity) registrationResponse);
        when(userClient.processPayment(eq(userId), eq(eventPrice))).thenReturn((ResponseEntity) paymentResponse);


        String result = bookingService.processEventPayment(userId, eventId);


        assertTrue(result.contains("Payment failed"));


        verify(eventClient).getEvent(eventId);
        verify(eventClient).isUserRegisteredForEvent(eventId, userId);
        verify(userClient).processPayment(userId, eventPrice);
        verify(eventClient, never()).addParticipantToEvent(any(), any());
        verify(userClient, never()).addEventToUser(any(), any());
    }

    @Test
    void processEventRefund_Success() {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("price", eventPrice);
        eventData.put("title", eventTitle);
        eventData.put("currency", eventCurrency);
        ResponseEntity<Map<String, Object>> eventResponse = new ResponseEntity<>(eventData, HttpStatus.OK);


        Map<String, Object> registrationCheckData = new HashMap<>();
        registrationCheckData.put("success", true);
        registrationCheckData.put("isRegistered", true);
        ResponseEntity<Map<String, Object>> registrationResponse = new ResponseEntity<>(registrationCheckData, HttpStatus.OK);


        Map<String, Object> refundData = new HashMap<>();
        refundData.put("success", true);
        refundData.put("balance", userBalance);
        ResponseEntity<Map<String, Object>> refundResponse = new ResponseEntity<>(refundData, HttpStatus.OK);


        Map<String, Object> balanceData = new HashMap<>();
        balanceData.put("success", true);
        balanceData.put("balance", userBalance); // After refund
        ResponseEntity<Map<String, Object>> balanceResponse = new ResponseEntity<>(balanceData, HttpStatus.OK);


        when(eventClient.getEvent(eq(eventId))).thenReturn((ResponseEntity) eventResponse);
        when(eventClient.isUserRegisteredForEvent(eq(eventId), eq(userId))).thenReturn((ResponseEntity) registrationResponse);
        when(userClient.processPayment(eq(userId), eq(-eventPrice))).thenReturn((ResponseEntity) refundResponse);
        when(userClient.getBalance(eq(userId))).thenReturn((ResponseEntity) balanceResponse);
        when(eventClient.removeParticipantFromEvent(eq(eventId), eq(userId))).thenReturn((ResponseEntity) new ResponseEntity<>(Map.of("success", true), HttpStatus.OK));
        when(userClient.removeEventFromUser(eq(userId), eq(eventId))).thenReturn((ResponseEntity) new ResponseEntity<>(Map.of("success", true), HttpStatus.OK));


        String result = bookingService.processEventRefund(userId, eventId);


        assertTrue(result.contains("Refund of " + eventPrice));
        assertTrue(result.contains(eventTitle));
        assertTrue(result.contains(String.valueOf(userBalance)));


        verify(eventClient).getEvent(eventId);
        verify(eventClient).isUserRegisteredForEvent(eventId, userId);
        verify(userClient).processPayment(userId, -eventPrice);
        verify(userClient).getBalance(userId);
        verify(eventClient).removeParticipantFromEvent(eventId, userId);
        verify(userClient).removeEventFromUser(userId, eventId);
    }

    @Test
    void processEventRefund_NotRegistered() {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("price", eventPrice);
        eventData.put("title", eventTitle);
        eventData.put("currency", eventCurrency);
        ResponseEntity<Map<String, Object>> eventResponse = new ResponseEntity<>(eventData, HttpStatus.OK);


        Map<String, Object> registrationCheckData = new HashMap<>();
        registrationCheckData.put("success", true);
        registrationCheckData.put("isRegistered", false);
        ResponseEntity<Map<String, Object>> registrationResponse = new ResponseEntity<>(registrationCheckData, HttpStatus.OK);


        when(eventClient.getEvent(eq(eventId))).thenReturn((ResponseEntity) eventResponse);
        when(eventClient.isUserRegisteredForEvent(eq(eventId), eq(userId))).thenReturn((ResponseEntity) registrationResponse);


        String result = bookingService.processEventRefund(userId, eventId);


        assertTrue(result.contains("not registered"));


        verify(eventClient).getEvent(eventId);
        verify(eventClient).isUserRegisteredForEvent(eventId, userId);
        verify(userClient, never()).processPayment(any(), any());
        verify(eventClient, never()).removeParticipantFromEvent(any(), any());
        verify(userClient, never()).removeEventFromUser(any(), any());
    }

    @Test
    void processEventRefund_FreeEvent() {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("price", 0.0);
        eventData.put("title", eventTitle);
        eventData.put("currency", eventCurrency);
        ResponseEntity<Map<String, Object>> eventResponse = new ResponseEntity<>(eventData, HttpStatus.OK);


        Map<String, Object> registrationCheckData = new HashMap<>();
        registrationCheckData.put("success", true);
        registrationCheckData.put("isRegistered", true);
        ResponseEntity<Map<String, Object>> registrationResponse = new ResponseEntity<>(registrationCheckData, HttpStatus.OK);


        when(eventClient.getEvent(eq(eventId))).thenReturn((ResponseEntity) eventResponse);
        when(eventClient.isUserRegisteredForEvent(eq(eventId), eq(userId))).thenReturn((ResponseEntity) registrationResponse);
        when(eventClient.removeParticipantFromEvent(eq(eventId), eq(userId))).thenReturn((ResponseEntity) new ResponseEntity<>(Map.of("success", true), HttpStatus.OK));
        when(userClient.removeEventFromUser(eq(userId), eq(eventId))).thenReturn((ResponseEntity) new ResponseEntity<>(Map.of("success", true), HttpStatus.OK));


        String result = bookingService.processEventRefund(userId, eventId);


        assertTrue(result.contains("Successfully unregistered from free event"));

        verify(eventClient).getEvent(eventId);
        verify(eventClient).isUserRegisteredForEvent(eventId, userId);
        verify(userClient, never()).processPayment(any(), any());
        verify(eventClient).removeParticipantFromEvent(eventId, userId);
        verify(userClient).removeEventFromUser(userId, eventId);
    }
}