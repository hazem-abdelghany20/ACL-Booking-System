package com.example.searchservice.strategies;

import com.example.searchservice.models.EventDto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class LocationSearchStrategy implements SearchStrategy {
    @Override
    public List<EventDto> filter(List<EventDto> events, String keyword, String location, LocalDate from, LocalDate to) {
        return events.stream()
                .filter(e -> location == null || e.getLocation().equalsIgnoreCase(location))
                .collect(Collectors.toList());
    }
}
