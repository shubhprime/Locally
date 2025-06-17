package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.client.EngineToUser.EngineToUserClient;
import com.locally.locally_backend_engine.dto.DeliveryPartnerToUserRatingRequest;
import com.locally.locally_backend_engine.dto.DeliveryPartnerToUserResponse;
import com.locally.locally_backend_engine.model.Delivery;
import com.locally.locally_backend_engine.model.DeliveryStatus;
import com.locally.locally_backend_engine.repository.DeliveryRepository;
import com.locally.locally_backend_engine.service.DeliveryPartnerRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryPartnerRatingServiceImpl implements DeliveryPartnerRatingService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private EngineToUserClient engineToUserClient;

    @Transactional
    @Override
    public DeliveryPartnerToUserResponse rateDelivery(DeliveryPartnerToUserRatingRequest deliveryPartnerToUserRatingRequest, String authHeader) {

        Delivery delivery = deliveryRepository.findById(deliveryPartnerToUserRatingRequest.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (!DeliveryStatus.PAID.equals(delivery.getDeliveryStatus())) {
            return DeliveryPartnerToUserResponse.builder()
                    .success(false)
                    .responseCode("400")
                    .responseMessage("Delivery must be marked as PAID before rating.")
                    .build();
        }

        if (delivery.isDeliveryPartnerHasRated()) {
            return DeliveryPartnerToUserResponse.builder()
                    .success(false)
                    .responseCode("409")
                    .responseMessage("Delivery partner has already rated this delivery.")
                    .deliveryId(delivery.getDeliveryId())
                    .build();
        }

        DeliveryPartnerToUserRatingRequest userRatingRequest = DeliveryPartnerToUserRatingRequest.builder()
                .userId(delivery.getSenderId())
                .newRating(deliveryPartnerToUserRatingRequest.getNewRating())
                .build();

        delivery.setDeliveryPartnerHasRated(true);
        deliveryRepository.save(delivery);

        // Forward to User service via Feign
        return engineToUserClient.addRating(userRatingRequest, authHeader);
    }
}