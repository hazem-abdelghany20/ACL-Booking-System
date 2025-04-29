package com.example.hotel.EventService;

import com.example.hotel.EventService.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class EventServiceApplicationTests {

    @Test
    void contextLoads() {
        // Simple test to verify that the Spring context loads
    }
    
    @Test
    void simpleTest() {
        // A simple test that always passes
        String expected = "hello";
        String actual = "hello";
        assertEquals(expected, actual, "Simple test to verify test setup");
    }
} 