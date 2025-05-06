package com.example.hotel.NotificationService.repositories;

import com.example.hotel.NotificationService.models.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdAndStatus(Long userId, String status);
}
