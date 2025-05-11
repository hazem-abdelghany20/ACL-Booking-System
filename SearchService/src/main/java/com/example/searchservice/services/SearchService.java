package com.example.searchservice.services;


import com.example.searchservice.models.EventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final RestTemplate restTemplate;

    @Value("${event.service.url}")
    private String eventServiceUrl;

    public SearchService() {
        this.restTemplate = new RestTemplate();
    }

    public List<EventDto> search(String keyword, String location, LocalDate fromDate, LocalDate toDate) {

        String requestUrl = eventServiceUrl + "/api/events/public?page=0&size=100";

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        List<?> eventsRaw = (List<?>) response.getBody().get("events");
        List<EventDto> events = mapRawToEvents(eventsRaw);

        return events.stream()
                .filter(event -> keyword == null ||
                        event.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        event.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .filter(event -> location == null || event.getLocation().equalsIgnoreCase(location))
                .filter(event -> fromDate == null || !event.getStartDateTime().toLocalDate().isBefore(fromDate))
                .filter(event -> toDate == null || !event.getStartDateTime().toLocalDate().isAfter(toDate))
                .sorted(Comparator.comparing(EventDto::getStartDateTime)) // Rank by date
                .collect(Collectors.toList());
    }

    private List<EventDto> mapRawToEvents(List<?> rawEvents) {
        // Simplified mapper for mock; replace with real mapping or use Jackson
        return rawEvents.stream()
                .map(obj -> {
                    Map<String, Object> map = (Map<String, Object>) obj;
                    EventDto dto = new EventDto();
                    dto.setId(Long.valueOf(map.get("id").toString()));
                    dto.setTitle(map.get("title").toString());
                    dto.setDescription(map.get("description").toString());
                    dto.setLocation(map.get("location").toString());
                    dto.setStartDateTime(LocalDateTime.parse(map.get("startDateTime").toString()));
                    dto.setEndDateTime(LocalDateTime.parse(map.get("endDateTime").toString()));
                    dto.setPrice(Double.parseDouble(map.get("price").toString()));
                    dto.setCurrency(map.get("currency").toString());
                    dto.setPublished(Boolean.parseBoolean(map.get("published").toString()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
