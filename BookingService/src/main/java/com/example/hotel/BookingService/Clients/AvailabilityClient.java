package com.example.hotel.BookingService.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "availability-service", url = "http://localhost:8091")
public interface AvailabilityClient {
    
    @GetMapping("/availability/check/{roomType}/{nights}")
    boolean check(@PathVariable String roomType, @PathVariable int nights);
}

