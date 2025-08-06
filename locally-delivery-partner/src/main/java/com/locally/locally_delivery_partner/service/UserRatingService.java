package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.DeliveryPartnerToUserRatingRequest;
import com.locally.locally_delivery_partner.dto.DeliveryPartnerToUserResponse;

public interface UserRatingService {
    DeliveryPartnerToUserResponse addRating(DeliveryPartnerToUserRatingRequest deliveryPartnerToUserRatingRequest, String authHeader);
}