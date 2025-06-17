package com.locally.backend.service.impl;

import com.locally.backend.dto.DeliveryPartnerToUserRequest;
import com.locally.backend.dto.DeliveryPartnerToUserResponse;
import com.locally.backend.model.User;
import com.locally.backend.repository.UserRepository;
import com.locally.backend.service.UserRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRatingServiceImpl implements UserRatingService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Override
    public DeliveryPartnerToUserResponse addRating(DeliveryPartnerToUserRequest deliveryPartnerToUserRequest) {
        User user = userRepository.findById(deliveryPartnerToUserRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        double currentTotalScore = user.getAverageRating() * user.getTotalRatings();
        int newTotalRatings = user.getTotalRatings() + 1;

        double newAverage = (currentTotalScore + deliveryPartnerToUserRequest.getNewRating()) / newTotalRatings;

        user.setTotalRatings(newTotalRatings);
        user.setAverageRating(newAverage);

        userRepository.save(user);
        return DeliveryPartnerToUserResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Rating submitted successfully")
                .build();
    }
}