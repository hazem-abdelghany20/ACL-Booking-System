package com.example.searchservice.strategies;

import com.example.searchservice.models.EventDto;
import java.util.List;
import java.time.LocalDate;

public interface SearchStrategy {
    List<EventDto> filter(List<EventDto> events, String keyword, String location, LocalDate from, LocalDate to);
}