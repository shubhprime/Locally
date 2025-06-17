package com.locally.backend.service;

import com.locally.backend.dto.DeliveryPartnerToUserRequest;
import com.locally.backend.dto.DeliveryPartnerToUserResponse;

public interface UserRatingService {
    public DeliveryPartnerToUserResponse addRating(DeliveryPartnerToUserRequest deliveryPartnerToUserRequest);
}