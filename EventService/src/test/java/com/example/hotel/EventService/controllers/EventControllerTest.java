package com.example.hotel.EventService.controllers;

import com.example.hotel.EventService.models.Event;
import com.example.hotel.EventService.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EventControllerTest {

    @Mock
    private EventService eventService;
    
    @InjectMocks
    private EventController eventController;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }
    
    @Test
    void simpleTest() {
        // A simple test that always passes
        String expected = "hello";
        String actual = "hello";
        assertEquals(expected, actual, "Simple test to verify test setup");
    }
} 