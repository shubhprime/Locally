package com.locally.locally_backend_engine.service;

import com.locally.locally_backend_engine.dto.DeliveryPartnerToUserRatingRequest;
import com.locally.locally_backend_engine.dto.DeliveryPartnerToUserResponse;

public interface DeliveryPartnerRatingService {
    public DeliveryPartnerToUserResponse rateDelivery(DeliveryPartnerToUserRatingRequest deliveryPartnerToUserRatingRequest, String authHeader);
}