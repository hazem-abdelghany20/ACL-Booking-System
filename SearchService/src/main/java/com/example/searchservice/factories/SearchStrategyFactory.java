package com.example.searchservice.factories;

import com.example.searchservice.strategies.DateSearchStrategy;
import com.example.searchservice.strategies.KeywordSearchStrategy;
import com.example.searchservice.strategies.LocationSearchStrategy;
import com.example.searchservice.strategies.SearchStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchStrategyFactory {
    public List<SearchStrategy> getStrategies() {
        return List.of(
                new KeywordSearchStrategy(),
                new LocationSearchStrategy(),
                new DateSearchStrategy()
        );
    }
}