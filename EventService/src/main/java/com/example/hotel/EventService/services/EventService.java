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
    public boolean addParticipantToEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));


        if (event.getParticipantIds().size() >= event.getCapacity()) {
            throw new RuntimeException("Event is at full capacity");
        }

        event.getParticipantIds().add(userId);
        eventRepository.save(event);

        return true;
    }

    @Transactional
    public boolean removeParticipantFromEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));


        if (!event.getParticipantIds().contains(userId)) {
            throw new RuntimeException("User is not a participant in this event");
        }


        event.getParticipantIds().remove(userId);
        eventRepository.save(event);

        return true;
    }

    public boolean isUserRegisteredForEvent(Long eventId, Long userId) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
            return event.getParticipantIds().contains(userId);

        } catch (Exception e) {
            System.err.println("Error checking if user is registered for event: " + e.getMessage());
            throw new RuntimeException("Error checking if user is registered for event: " + e.getMessage(), e);
        }
    }


} 