package com.locally.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locally.backend.client.UserToEngine.UserToEngineClient;
import com.locally.backend.dto.UserToDeliveryPartnerRequest;
import com.locally.backend.dto.UserToDeliveryPartnerResponse;
import com.locally.backend.model.User;
import com.locally.backend.repository.UserRepository;
import com.locally.backend.service.DeliveryPartnerRatingService;
import com.locally.backend.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryPartnerRatingServiceImpl implements DeliveryPartnerRatingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserToEngineClient userToEngineClient;

    @Transactional
    @Override
    public UserToDeliveryPartnerResponse addRating(UserToDeliveryPartnerRequest userToDeliveryPartnerRequest, String authHeader) {

        User user = userRepository.findByEmail(userToDeliveryPartnerRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        if (userToDeliveryPartnerRequest.getNewRating() < 1 || userToDeliveryPartnerRequest.getNewRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        try {
            return userToEngineClient.rateDelivery(userToDeliveryPartnerRequest, authHeader);
        } catch (feign.FeignException fe) {
            // Extract JSON body from FeignException and convert it
            try {
                String errorBody = fe.contentUTF8();
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(errorBody, UserToDeliveryPartnerResponse.class);
            } catch (Exception e) {
                return UserToDeliveryPartnerResponse.builder()
                        .success(false)
                        .responseCode(String.valueOf(fe.status()))
                        .responseMessage("Internal error or malformed error response from Engine")
                        .build();
            }
        }
    }
}