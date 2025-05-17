package com.example.searchservice.strategies;

import com.example.searchservice.models.EventDto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class KeywordSearchStrategy implements SearchStrategy {
    @Override
    public List<EventDto> filter(List<EventDto> events, String keyword, String location, LocalDate from, LocalDate to) {
        return events.stream()
                .filter(e -> keyword == null ||
                        e.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        e.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
}
