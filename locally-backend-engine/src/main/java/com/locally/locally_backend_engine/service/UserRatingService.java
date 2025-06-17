package com.locally.locally_backend_engine.service;

import com.locally.locally_backend_engine.dto.UserToDeliveryPartnerRequest;
import com.locally.locally_backend_engine.dto.UserToDeliveryPartnerResponse;

public interface UserRatingService {
    public UserToDeliveryPartnerResponse rateDelivery(UserToDeliveryPartnerRequest userToDeliveryPartnerRequest, String authHeader);
}