package com.example.hotel.BookingService.services;

import com.example.hotel.BookingService.Clients.EventClient;
import com.example.hotel.BookingService.Clients.UserClient;
import com.example.hotel.BookingService.Clients.AvailabilityClient;
import com.example.hotel.BookingService.Entities.Booking;
import com.example.hotel.BookingService.Entities.BookingStatus;
import com.example.hotel.BookingService.Repositories.BookingRepository;
import com.example.hotel.BookingService.Services.BookingService;
import com.example.hotel.BookingService.rabbitmq.BookingProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

class BookingServiceTest {

    @Mock private EventClient eventClient;
    @Mock private UserClient userClient;
    @Mock private AvailabilityClient availabilityClient;
    @Mock private BookingProducer producer;
    @Mock private BookingRepository bookingRepository;
    @InjectMocks private BookingService bookingService;

    // If you set these via @Value in test, alternatively hardcode matching your application.yml
    private final String name = "TestName";
    private final String id = "TestId";
    private static final String DEFAULT_AUTH_TOKEN = "Bearer default-token";

    private Long eventId = 1L;
    private Long userId = 2L;
    private String userIdStr = "2";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(bookingService, "name", "TestName");
        ReflectionTestUtils.setField(bookingService, "id", "TestId");
    }

    @Test
    void shouldCreateBooking_whenTicketsAvailable() {
        when(eventClient.getAvailableTickets(eventId))
                .thenReturn(Map.of("availableTickets", 5));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String bookingId = bookingService.createEventBooking(eventId, userId);

        assertNotNull(bookingId);
        verify(bookingRepository).save(argThat(b ->
                b.getId().equals(bookingId) &&
                        b.getEventId().equals(eventId) &&
                        b.getUserId().equals(userId) &&
                        b.getStatus() == BookingStatus.CONFIRMED
        ));
        verify(eventClient).adjustAvailableTickets(eventId, -1);
        verify(producer).sendBooking(bookingId + " " + name + "_" + id);
    }

    @Test
    void shouldThrow_whenNoTicketsAvailable() {
        when(eventClient.getAvailableTickets(eventId))
                .thenReturn(Map.of("availableTickets", 0));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                bookingService.createEventBooking(eventId, userId)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(bookingRepository, producer);
    }

    @Test
    void shouldCancelBooking_whenConfirmed() {
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, eventId, userId, BookingStatus.CONFIRMED);
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        bookingService.cancelBooking(bookingId);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(eventClient).adjustAvailableTickets(eventId, +1);
        verify(producer).sendBooking(bookingId + " CANCELLED");
    }

    @Test
    void shouldThrowOnCancel_nonexistent() {
        String bookingId = "not-found";
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                bookingService.cancelBooking(bookingId)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void shouldThrowOnCancel_wrongStatus() {
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, eventId, userId, BookingStatus.REFUNDED);
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                bookingService.cancelBooking(bookingId)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldRefundBooking_whenCancelled() {
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, eventId, userId, BookingStatus.CANCELLED);
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        bookingService.refundBooking(bookingId);

        assertEquals(BookingStatus.REFUNDED, booking.getStatus());
        verify(producer).sendBooking(bookingId + " REFUNDED");
    }

    @Test
    void shouldThrowOnRefund_nonexistent() {
        String bookingId = "no";
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                bookingService.refundBooking(bookingId)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void shouldThrowOnRefund_wrongStatus() {
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, eventId, userId, BookingStatus.CONFIRMED);
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                bookingService.refundBooking(bookingId)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // New tests for payment processing functionality

    @Test
    void shouldProcessPayment_forPaidEvent() {
        // Setup mock responses
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "Paid Event");
        eventData.put("price", 50.0);
        eventData.put("currency", "USD");

        // Use thenAnswer to avoid generic type issues
        when(eventClient.getEvent(eventId))
                .thenAnswer(invocation -> ResponseEntity.ok(eventData));

        Map<String, Object> registrationStatus = new HashMap<>();
        registrationStatus.put("isRegistered", false);

        // Use thenAnswer with Object type
        when(eventClient.isUserRegisteredForEvent(eq(eventId), eq(userId)))
                .thenAnswer(invocation -> {
                    Object body = registrationStatus;
                    return ResponseEntity.ok(body);
                });

        // Instead of doNothing(), use when().thenReturn() with appropriate return value
        when(eventClient.addParticipantToEvent(eq(eventId), eq(userId)))
                .thenReturn(ResponseEntity.ok().build());

        // User client mocks
        Map<String, Object> deductResponse = new HashMap<>();
        deductResponse.put("userId", userIdStr);
        deductResponse.put("balance", 950.0);

        // Use thenAnswer to avoid generic type issues
        when(userClient.deductFunds(eq(userIdStr), eq(50.0), eq(DEFAULT_AUTH_TOKEN)))
                .thenAnswer(invocation -> ResponseEntity.ok(deductResponse));

        Map<String, Object> balanceResponse = new HashMap<>();
        balanceResponse.put("userId", userIdStr);
        balanceResponse.put("balance", 950.0);

        when(userClient.getWalletBalance(eq(userIdStr), eq(DEFAULT_AUTH_TOKEN)))
                .thenAnswer(invocation -> ResponseEntity.ok(balanceResponse));

        // Instead of doNothing(), use when().thenReturn() with appropriate return value
        when(userClient.addEventToUser(eq(userId), eq(eventId)))
                .thenReturn(ResponseEntity.ok().build());

        // Execute test
        String result = bookingService.processEventPayment(userId, eventId);

        // Verify
        verify(userClient).deductFunds(eq(userIdStr), eq(50.0), eq(DEFAULT_AUTH_TOKEN));
        verify(eventClient).addParticipantToEvent(eq(eventId), eq(userId));
        verify(userClient).addEventToUser(eq(userId), eq(eventId));
        verify(userClient).getWalletBalance(eq(userIdStr), eq(DEFAULT_AUTH_TOKEN));

        assertTrue(result.contains("Payment processed successfully"));
        assertTrue(result.contains("950.0"));
    }

    @Test
    void shouldRegisterForFreeEvent() {
        // Setup mock responses
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "Free Event");
        eventData.put("price", 0.0);

        // Use thenAnswer to avoid generic type issues
        when(eventClient.getEvent(eventId))
                .thenAnswer(invocation -> ResponseEntity.ok(eventData));

        Map<String, Object> registrationStatus = new HashMap<>();
        registrationStatus.put("isRegistered", false);

        // Use thenAnswer with Object type
        when(eventClient.isUserRegisteredForEvent(eventId, userId))
                .thenAnswer(invocation -> {
                    Object body = registrationStatus;
                    return ResponseEntity.ok(body);
                });

        // Instead of doNothing(), use when().thenReturn() with appropriate return value
        when(eventClient.addParticipantToEvent(eq(eventId), eq(userId)))
                .thenReturn(ResponseEntity.ok().build());

        when(userClient.addEventToUser(eq(userId), eq(eventId)))
                .thenReturn(ResponseEntity.ok().build());

        // Execute test
        String result = bookingService.processEventPayment(userId, eventId);

        // Verify
        verify(eventClient).addParticipantToEvent(eq(eventId), eq(userId));
        verify(userClient).addEventToUser(eq(userId), eq(eventId));
        verify(userClient, never()).deductFunds(anyString(), anyDouble(), anyString());

        assertTrue(result.contains("Successfully registered for free event"));
    }

    @Test
    void shouldReturnAlreadyRegistered_whenUserAlreadyRegistered() {
        // Setup mock responses
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "Test Event");
        eventData.put("price", 25.0);

        // Use thenAnswer to avoid generic type issues
        when(eventClient.getEvent(eventId))
                .thenAnswer(invocation -> ResponseEntity.ok(eventData));

        Map<String, Object> registrationStatus = new HashMap<>();
        registrationStatus.put("isRegistered", true);

        // Use thenAnswer with Object type
        when(eventClient.isUserRegisteredForEvent(eventId, userId))
                .thenAnswer(invocation -> {
                    Object body = registrationStatus;
                    return ResponseEntity.ok(body);
                });

        // Execute test
        String result = bookingService.processEventPayment(userId, eventId);

        // Verify
        verify(eventClient, never()).addParticipantToEvent(any(), any());
        verify(userClient, never()).deductFunds(any(), any(), any());

        assertTrue(result.contains("already registered"));
    }

    @Test
    void shouldProcessRefund_forPaidEvent() {
        // Setup mock responses
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "Paid Event");
        eventData.put("price", 50.0);
        eventData.put("currency", "USD");

        // Use thenAnswer to avoid generic type issues
        when(eventClient.getEvent(eventId))
                .thenAnswer(invocation -> ResponseEntity.ok(eventData));

        Map<String, Object> registrationStatus = new HashMap<>();
        registrationStatus.put("isRegistered", true);

        // Use thenAnswer with Object type
        when(eventClient.isUserRegisteredForEvent(eventId, userId))
                .thenAnswer(invocation -> {
                    Object body = registrationStatus;
                    return ResponseEntity.ok(body);
                });

        // Instead of doNothing(), use when().thenReturn() with appropriate return value
        when(eventClient.removeParticipantFromEvent(eq(eventId), eq(userId)))
                .thenReturn(ResponseEntity.ok().build());

        // User client mocks
        Map<String, Object> addFundsResponse = new HashMap<>();
        addFundsResponse.put("userId", userIdStr);
        addFundsResponse.put("balance", 1050.0);

        // Use thenAnswer to avoid generic type issues
        when(userClient.addFunds(eq(userIdStr), eq(50.0), eq(DEFAULT_AUTH_TOKEN)))
                .thenAnswer(invocation -> ResponseEntity.ok(addFundsResponse));

        Map<String, Object> balanceResponse = new HashMap<>();
        balanceResponse.put("userId", userIdStr);
        balanceResponse.put("balance", 1050.0);

        when(userClient.getWalletBalance(eq(userIdStr), eq(DEFAULT_AUTH_TOKEN)))
                .thenAnswer(invocation -> ResponseEntity.ok(balanceResponse));

        // Instead of doNothing(), use when().thenReturn() with appropriate return value
        when(userClient.removeEventFromUser(eq(userId), eq(eventId)))
                .thenReturn(ResponseEntity.ok().build());

        // Execute test
        String result = bookingService.processEventRefund(userId, eventId);

        // Verify
        verify(userClient).addFunds(eq(userIdStr), eq(50.0), eq(DEFAULT_AUTH_TOKEN));
        verify(eventClient).removeParticipantFromEvent(eq(eventId), eq(userId));
        verify(userClient).removeEventFromUser(eq(userId), eq(eventId));
        verify(userClient).getWalletBalance(eq(userIdStr), eq(DEFAULT_AUTH_TOKEN));

        assertTrue(result.contains("Refund of 50.0"));
        assertTrue(result.contains("1050.0"));
    }

    @Test
    void shouldHandleUnregistration_fromFreeEvent() {
        // Setup mock responses
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "Free Event");
        eventData.put("price", 0.0);

        // Use thenAnswer to avoid generic type issues
        when(eventClient.getEvent(eventId))
                .thenAnswer(invocation -> ResponseEntity.ok(eventData));

        Map<String, Object> registrationStatus = new HashMap<>();
        registrationStatus.put("isRegistered", true);

        // Use thenAnswer with Object type
        when(eventClient.isUserRegisteredForEvent(eventId, userId))
                .thenAnswer(invocation -> {
                    Object body = registrationStatus;
                    return ResponseEntity.ok(body);
                });

        // Instead of doNothing(), use when().thenReturn() with appropriate return value
        when(eventClient.removeParticipantFromEvent(eq(eventId), eq(userId)))
                .thenReturn(ResponseEntity.ok().build());

        when(userClient.removeEventFromUser(eq(userId), eq(eventId)))
                .thenReturn(ResponseEntity.ok().build());

        // Execute test
        String result = bookingService.processEventRefund(userId, eventId);

        // Verify
        verify(eventClient).removeParticipantFromEvent(eq(eventId), eq(userId));
        verify(userClient).removeEventFromUser(eq(userId), eq(eventId));
        verify(userClient, never()).addFunds(any(), any(), any());

        assertTrue(result.contains("Successfully unregistered from free event"));
    }

    @Test
    void shouldHandlePaymentFailure_whenInsufficientFunds() {
        // Setup mock responses
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "Expensive Event");
        eventData.put("price", 5000.0);

        // Use thenAnswer to avoid generic type issues
        when(eventClient.getEvent(eventId))
                .thenAnswer(invocation -> ResponseEntity.ok(eventData));

        Map<String, Object> registrationStatus = new HashMap<>();
        registrationStatus.put("isRegistered", false);

        // Use thenAnswer with Object type
        when(eventClient.isUserRegisteredForEvent(eventId, userId))
                .thenAnswer(invocation -> {
                    Object body = registrationStatus;
                    return ResponseEntity.ok(body);
                });

        // User client mocks - payment fails
        when(userClient.deductFunds(eq(userIdStr), eq(5000.0), eq(DEFAULT_AUTH_TOKEN)))
                .thenAnswer(invocation -> ResponseEntity.badRequest().build());

        // Execute test
        String result = bookingService.processEventPayment(userId, eventId);

        // Verify
        verify(userClient).deductFunds(eq(userIdStr), eq(5000.0), eq(DEFAULT_AUTH_TOKEN));
        verify(eventClient, never()).addParticipantToEvent(any(), any());

        assertTrue(result.contains("Payment failed"));
}
}