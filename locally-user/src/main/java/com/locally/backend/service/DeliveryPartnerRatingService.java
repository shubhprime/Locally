package com.locally.backend.service;

import com.locally.backend.dto.UserToDeliveryPartnerRequest;
import com.locally.backend.dto.UserToDeliveryPartnerResponse;

public interface DeliveryPartnerRatingService {
    public UserToDeliveryPartnerResponse addRating(UserToDeliveryPartnerRequest userToDeliveryPartnerRequest, String authHeader);
}