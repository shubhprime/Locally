package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.UserToDeliveryPartnerRequest;
import com.locally.locally_delivery_partner.dto.UserToDeliveryPartnerResponse;

public interface DeliveryPartnerRatingService {
    public UserToDeliveryPartnerResponse addRating(UserToDeliveryPartnerRequest userToDeliveryPartnerRequest);
}