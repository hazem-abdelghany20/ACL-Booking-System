package com.example.hotel.BookingService.Services;

import com.example.hotel.BookingService.Clients.EventClient;
import com.example.hotel.BookingService.Entities.Booking;
import com.example.hotel.BookingService.Entities.BookingStatus;
import com.example.hotel.BookingService.rabbitmq.BookingProducer;
import com.example.hotel.BookingService.Repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class BookingService {

    private final EventClient eventClient;
    private final BookingProducer producer;
    private final BookingRepository bookingRepository;

    @Value("${Name}") private String name;
    @Value("${ID}")   private String id;

    public BookingService(EventClient eventClient,
                          BookingProducer producer,
                          BookingRepository bookingRepository) {
        this.eventClient = eventClient;
        this.producer = producer;
        this.bookingRepository = bookingRepository;
    }

     // Create a new booking: check availability, persist, decrement tickets, notify.

    public String createEventBooking(Long eventId, Long userId) {
        // Check ticket availability
        @SuppressWarnings("unchecked")
        Map<String, Object> avail = eventClient.getAvailableTickets(eventId);
        int available = (int) avail.get("availableTickets");
        if (available <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No tickets available for event " + eventId);
        }

        // Persist booking
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, eventId, userId, BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Decrement tickets on the event
        eventClient.adjustAvailableTickets(eventId, -1);

        // Send notification
        producer.sendBooking(bookingId + " " + name + "_" + id);
        return bookingId;
    }


     // Cancel an existing booking: only CONFIRMED → CANCELLED, free a ticket, notify.

    public void cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found: " + bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only CONFIRMED bookings can be cancelled");
        }

        // free up one ticket
        eventClient.adjustAvailableTickets(booking.getEventId(), +1);

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        producer.sendBooking(bookingId + " CANCELLED");
    }

     //Refund a booking: only CANCELLED → REFUNDED, no ticket adjustment, notify.

    public void refundBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found: " + bookingId));

        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only CANCELLED bookings can be refunded");
        }

        booking.setStatus(BookingStatus.REFUNDED);
        bookingRepository.save(booking);

        producer.sendBooking(bookingId + " REFUNDED");
    }
}

