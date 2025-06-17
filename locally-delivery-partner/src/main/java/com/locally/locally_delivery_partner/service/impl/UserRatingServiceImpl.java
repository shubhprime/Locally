package com.locally.locally_delivery_partner.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locally.locally_delivery_partner.client.DeliveryPartnerToEngine.DeliveryPartnerToEngineClient;
import com.locally.locally_delivery_partner.dto.DeliveryPartnerToUserRatingRequest;
import com.locally.locally_delivery_partner.dto.DeliveryPartnerToUserResponse;
import com.locally.locally_delivery_partner.model.DeliveryPartner;
import com.locally.locally_delivery_partner.repository.DeliveryPartnerRepository;
import com.locally.locally_delivery_partner.service.UserRatingService;
import com.locally.locally_delivery_partner.utils.DeliveryPartnerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserRatingServiceImpl implements UserRatingService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private DeliveryPartnerToEngineClient  deliveryPartnerToEngineClient;

    @Override
    public DeliveryPartnerToUserResponse addRating(DeliveryPartnerToUserRatingRequest deliveryPartnerToUserRatingRequest, String authHeader) {

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(deliveryPartnerToUserRatingRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        if (deliveryPartnerToUserRatingRequest.getNewRating() < 1 || deliveryPartnerToUserRatingRequest.getNewRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        try {
            return deliveryPartnerToEngineClient.rateDelivery(deliveryPartnerToUserRatingRequest, authHeader);
        } catch (feign.FeignException fe) {
            // Extract JSON body from FeignException and convert it
            try {
                String errorBody = fe.contentUTF8();
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(errorBody, DeliveryPartnerToUserResponse.class);
            } catch (Exception e) {
                return DeliveryPartnerToUserResponse.builder()
                        .success(false)
                        .responseCode(String.valueOf(fe.status()))
                        .responseMessage("Internal error or malformed error response from Engine")
                        .build();
            }
        }
    }
}