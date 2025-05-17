package com.example.hotel.BookingService.Repositories;

import com.example.hotel.BookingService.Entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, String> {
}
