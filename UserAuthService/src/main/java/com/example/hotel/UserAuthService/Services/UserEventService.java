package com.example.hotel.UserAuthService.Services;

import com.example.hotel.UserAuthService.models.User;
import com.example.hotel.UserAuthService.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserEventService {

    @Autowired
    private UserRepository userRepository;


    @Transactional
    public boolean addEventToUser(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));


        if (user.getEventIds() == null) {
            throw new RuntimeException("User events collection is null");
        }

        user.getEventIds().add(eventId);
        userRepository.save(user);

        return true;
    }


    @Transactional
    public boolean removeEventFromUser(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));


        if (user.getEventIds() == null || !user.getEventIds().contains(eventId)) {
            throw new RuntimeException("User is not registered for this event");
        }

        user.getEventIds().remove(eventId);
        userRepository.save(user);

        return true;
    }

}