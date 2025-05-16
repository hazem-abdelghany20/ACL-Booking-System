package com.example.hotel.UserAuthService.Services;

import com.example.hotel.UserAuthService.models.User;
import com.example.hotel.UserAuthService.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {
    @Autowired
    private UserRepository userRepository;


    @Transactional
    public boolean processPayment(Long userId, Double amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getBalance() >= amount) {
            user.setBalance(user.getBalance() - amount);
            userRepository.save(user);
            return true;
        }
        return false;
    }


    @Transactional
    public Double depositToWallet(Long userId, Double amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setBalance(user.getBalance() + amount);
        userRepository.save(user);
        return user.getBalance();
    }


    public Double getBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return user.getBalance();
    }
}
