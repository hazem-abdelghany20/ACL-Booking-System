package com.example.hotel.EventService.repositories;

import com.example.hotel.EventService.models.Event;
import com.example.hotel.EventService.models.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByOrganizerId(Long organizerId);
    
    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);
    
    Page<Event> findByEventTypeAndIsPublishedTrue(EventType eventType, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.startDateTime >= :startDate AND e.eventType = 'PUBLIC' AND e.isPublished = true")
    Page<Event> findUpcomingPublicEvents(@Param("startDate") LocalDateTime startDate, Pageable pageable);
    
    @Query("SELECT e FROM Event e JOIN e.categories c WHERE c.id = :categoryId AND e.eventType = 'PUBLIC' AND e.isPublished = true")
    Page<Event> findByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) AND e.eventType = 'PUBLIC' AND e.isPublished = true")
    Page<Event> searchEvents(@Param("keyword") String keyword, Pageable pageable);
} 