package com.example.hotel.EventService.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotNull
    private LocalDateTime startDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    @NotNull
    @Column(nullable = false)
    private Integer availableTickets;

    @NotBlank
    @Size(max = 200)
    private String location;

    @NotNull
    private Integer capacity;

    private String imageUrl;

    @NotNull
    private Long organizerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "event_categories",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    private boolean isPublished = false;
    
    private double price = 0.0;
    
    private String currency = "USD";

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "event_participants", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "participant_id")
    private Set<Long> participantIds = new HashSet<>();

    public Event() {
    }

    public Event(String title, String description, LocalDateTime startDateTime, LocalDateTime endDateTime,
                String location, Integer capacity, Long organizerId, EventType eventType) {
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.capacity = capacity;
        this.organizerId = organizerId;
        this.eventType = eventType;
        this.availableTickets = this.capacity;

    }

    // Builder pattern implementation
    public static class Builder {
        private Event event = new Event();

        public Builder title(String title) {
            event.setTitle(title);
            return this;
        }

        public Builder description(String description) {
            event.setDescription(description);
            return this;
        }

        public Builder startDateTime(LocalDateTime startDateTime) {
            event.setStartDateTime(startDateTime);
            return this;
        }

        public Builder endDateTime(LocalDateTime endDateTime) {
            event.setEndDateTime(endDateTime);
            return this;
        }

        public Builder location(String location) {
            event.setLocation(location);
            return this;
        }

        public Builder capacity(Integer capacity) {
            event.setCapacity(capacity);
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            event.setImageUrl(imageUrl);
            return this;
        }

        public Builder organizerId(Long organizerId) {
            event.setOrganizerId(organizerId);
            return this;
        }

        public Builder eventType(EventType eventType) {
            event.setEventType(eventType);
            return this;
        }
        
        public Builder published(boolean isPublished) {
            event.setPublished(isPublished);
            return this;
        }
        
        public Builder price(double price) {
            event.setPrice(price);
            return this;
        }
        
        public Builder currency(String currency) {
            event.setCurrency(currency);
            return this;
        }

        public Builder availableTickets(Integer avail) {
            event.setAvailableTickets(avail);
            return this;
        }


        public Event build() {
            return event;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Set<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(Set<Long> participantIds) {
        this.participantIds = participantIds;
    }

    public Integer getAvailableTickets() {
        return availableTickets;
    }
    public void setAvailableTickets(Integer availableTickets) {
        this.availableTickets = availableTickets;
    }
} 