package com.example.hotel.EventService.controllers;

import com.example.hotel.EventService.models.Category;
import com.example.hotel.EventService.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class CategoryControllerTest {

    @Mock
    private EventService eventService;
    
    @InjectMocks
    private CategoryController categoryController;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }
    
    @Test
    void simpleTest() {
        // A simple test that always passes
        String expected = "hello";
        String actual = "hello";
        assertEquals(expected, actual, "Simple test to verify test setup");
    }
    
    @Test
    void getAllCategoriesSimpleTest() {
        // Mock data
        List<Category> mockCategories = new ArrayList<>();
        when(eventService.getAllCategories()).thenReturn(mockCategories);
        
        // Test
        ResponseEntity<List<Category>> response = categoryController.getAllCategories();
        
        // Verify
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockCategories, response.getBody());
    }
} 