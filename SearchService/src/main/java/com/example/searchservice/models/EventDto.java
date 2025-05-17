package com.example.searchservice.models;


import java.time.LocalDateTime;
import java.util.Set;

public class EventDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private double price;
    private String currency;
    private boolean isPublished;
    private Set<String> categories;

    public EventDto() {}

    // Getters and setters...

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }

    public Set<String> getCategories() { return categories; }
    public void setCategories(Set<String> categories) { this.categories = categories; }
}

