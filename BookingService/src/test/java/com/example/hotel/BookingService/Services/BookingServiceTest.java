package com.example.hotel.BookingService.Services;

import com.example.hotel.BookingService.Clients.EventClient;
import com.example.hotel.BookingService.Entities.Booking;
import com.example.hotel.BookingService.Entities.BookingStatus;
import com.example.hotel.BookingService.Repositories.BookingRepository;
import com.example.hotel.BookingService.rabbitmq.BookingProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;


class BookingServiceTest {

    @Mock private EventClient eventClient;
    @Mock private BookingProducer producer;
    @Mock private BookingRepository bookingRepository;
    @InjectMocks private BookingService bookingService;

    // If you set these via @Value in test, alternatively hardcode matching your application.yml
    private final String name = "TestName";
    private final String id = "TestId";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(bookingService, "name", "TestName");
        ReflectionTestUtils.setField(bookingService, "id",   "TestId");
    }

    @Test
    void shouldCreateBooking_whenTicketsAvailable() {
        Long eventId = 1L;
        Long userId = 2L;
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
        Long eventId = 1L;
        Long userId = 2L;
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
        Booking booking = new Booking(bookingId, 1L, 2L, BookingStatus.CONFIRMED);
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        bookingService.cancelBooking(bookingId);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(eventClient).adjustAvailableTickets(1L, +1);
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
        Booking booking = new Booking(bookingId, 1L, 2L, BookingStatus.REFUNDED);
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
        Booking booking = new Booking(bookingId, 1L, 2L, BookingStatus.CANCELLED);
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
        Booking booking = new Booking(bookingId, 1L, 2L, BookingStatus.CONFIRMED);
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                bookingService.refundBooking(bookingId)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
