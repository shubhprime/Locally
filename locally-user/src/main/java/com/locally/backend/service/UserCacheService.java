package com.locally.backend.service;

import com.locally.backend.model.User;
import com.locally.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserCacheService {

    @Autowired
    private UserRepository userRepository;

    @Cacheable(value = "users", key = "#email")
    public Optional<User> getCachedUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Cacheable(value = "users", key = "#phoneNumber")
    public Optional<User> getCachedUserByPhone(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }
}