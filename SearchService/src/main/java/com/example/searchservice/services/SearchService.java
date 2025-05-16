package com.example.searchservice.services;


import com.example.searchservice.factories.SearchStrategyFactory;
import com.example.searchservice.models.EventDto;
import com.example.searchservice.strategies.SearchStrategy;
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

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${event.service.url}")
    private String eventServiceUrl;

    private final SearchStrategyFactory strategyFactory;

    public SearchService(SearchStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
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

        // Apply all strategies
        for (SearchStrategy strategy : strategyFactory.getStrategies()) {
            events = strategy.filter(events, keyword, location, fromDate, toDate);
        }

        return events.stream()
                .sorted(Comparator.comparing(EventDto::getStartDateTime))
                .collect(Collectors.toList());
    }

    private List<EventDto> mapRawToEvents(List<?> rawEvents) {
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
