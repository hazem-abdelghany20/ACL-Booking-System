package com.example.hotel.EventService.services;

import com.example.hotel.EventService.exceptions.ResourceNotFoundException;
import com.example.hotel.EventService.models.Category;
import com.example.hotel.EventService.models.Event;
import com.example.hotel.EventService.models.EventType;
import com.example.hotel.EventService.repositories.CategoryRepository;
import com.example.hotel.EventService.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.hotel.EventService.clients.PaymentClient;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PaymentClient paymentClient;
    
    // Event CRUD Operations
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
    
    public Page<Event> getEventsPaginated(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }
    
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }
    
    @Transactional
    public Event createEvent(Event event, List<String> categoryNames) {
        // Process categories
        if (categoryNames != null && !categoryNames.isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (String categoryName : categoryNames) {
                Category category = categoryRepository.findByName(categoryName)
                        .orElseGet(() -> {
                            Category newCategory = new Category(categoryName);
                            return categoryRepository.save(newCategory);
                        });
                categories.add(category);
            }
            event.setCategories(categories);
        }
        
        return eventRepository.save(event);
    }
    
    @Transactional
    public Event updateEvent(Long id, Event eventDetails, List<String> categoryNames) {
        Event event = getEventById(id);
        
        // Update event fields
        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setStartDateTime(eventDetails.getStartDateTime());
        event.setEndDateTime(eventDetails.getEndDateTime());
        event.setLocation(eventDetails.getLocation());
        event.setCapacity(eventDetails.getCapacity());
        event.setImageUrl(eventDetails.getImageUrl());
        event.setEventType(eventDetails.getEventType());
        event.setPublished(eventDetails.isPublished());
        event.setPrice(eventDetails.getPrice());
        event.setCurrency(eventDetails.getCurrency());
        
        // Process categories
        if (categoryNames != null && !categoryNames.isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (String categoryName : categoryNames) {
                Category category = categoryRepository.findByName(categoryName)
                        .orElseGet(() -> {
                            Category newCategory = new Category(categoryName);
                            return categoryRepository.save(newCategory);
                        });
                categories.add(category);
            }
            event.setCategories(categories);
        }
        
        return eventRepository.save(event);
    }
    
    public void deleteEvent(Long id) {
        Event event = getEventById(id);
        eventRepository.delete(event);
    }
    
    // Specialized search operations
    public Page<Event> getEventsByOrganizer(Long organizerId, Pageable pageable) {
        return eventRepository.findByOrganizerId(organizerId, pageable);
    }
    
    public Page<Event> getPublicEvents(Pageable pageable) {
        return eventRepository.findByEventTypeAndIsPublishedTrue(EventType.PUBLIC, pageable);
    }
    
    public Page<Event> getUpcomingEvents(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findUpcomingPublicEvents(now, pageable);
    }
    
    public Page<Event> getEventsByCategory(Long categoryId, Pageable pageable) {
        return eventRepository.findByCategory(categoryId, pageable);
    }
    
    public Page<Event> searchEvents(String keyword, Pageable pageable) {
        return eventRepository.searchEvents(keyword, pageable);
    }
    
    // Event status operations
    @Transactional
    public Event publishEvent(Long id) {
        Event event = getEventById(id);
        event.setPublished(true);
        return eventRepository.save(event);
    }
    
    @Transactional
    public Event unpublishEvent(Long id) {
        Event event = getEventById(id);
        event.setPublished(false);
        return eventRepository.save(event);
    }
    
    @Transactional
    public Event changeEventType(Long id, EventType newType) {
        Event event = getEventById(id);
        event.setEventType(newType);
        return eventRepository.save(event);
    }
    
    // Category operations
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
    
    @Transactional
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
    
    @Transactional
    public Event signUpUserToEvent(Long eventId, Long userId) {
        Event event = getEventById(eventId);
        if (event.getParticipantIds().contains(userId)) {
            throw new IllegalStateException("User already signed up for this event.");
        }
        if (event.getParticipantIds().size() >= event.getCapacity()) {
            throw new IllegalStateException("Event is full.");
        }
        event.getParticipantIds().add(userId);
        return eventRepository.save(event);
    }

    @Transactional
    public String processEventPayment(Long eventId, Long userId) {
        // Check if event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + eventId));

        // Check for invalid price
        if (event.getPrice() < 0) {
            throw new IllegalStateException("Invalid event price: " + event.getPrice());
        }

        // Handle free event
        if (event.getPrice() == 0) {
            return "No payment required for free event";
        }

        // Process payment
        try {
            ResponseEntity<Map<String, Object>> response =
                    paymentClient.processPayment(userId, event.getPrice());

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !(Boolean)responseBody.get("success")) {
                return "Payment failed: Insufficient funds";
            }

            // Payment successful - get the remaining balance from the response
            Double remainingBalance = (Double) responseBody.get("remainingBalance");

            // Return success message with balance information
            return "Payment processed successfully for event: " + event.getTitle() +
                    ". Your new balance is: " + remainingBalance;

        } catch (Exception e) {
            throw new IllegalStateException("Error processing payment: " + e.getMessage());
        }
    }


} 