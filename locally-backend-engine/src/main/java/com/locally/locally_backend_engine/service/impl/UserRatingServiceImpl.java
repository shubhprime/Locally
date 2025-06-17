package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.client.EngineToDeliveryPartner.EngineToDeliveryPartnerClient;
import com.locally.locally_backend_engine.dto.UserToDeliveryPartnerRequest;
import com.locally.locally_backend_engine.dto.UserToDeliveryPartnerResponse;
import com.locally.locally_backend_engine.model.Delivery;
import com.locally.locally_backend_engine.model.DeliveryStatus;
import com.locally.locally_backend_engine.repository.DeliveryRepository;
import com.locally.locally_backend_engine.service.UserRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRatingServiceImpl implements UserRatingService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private EngineToDeliveryPartnerClient engineToDeliveryPartnerClient;

    @Transactional
    @Override
    public UserToDeliveryPartnerResponse rateDelivery(UserToDeliveryPartnerRequest userToDeliveryPartnerRequest, String authHeader) {

        Delivery delivery = deliveryRepository.findById(userToDeliveryPartnerRequest.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (!DeliveryStatus.PAID.equals(delivery.getDeliveryStatus())) {
            return UserToDeliveryPartnerResponse.builder()
                    .success(false)
                    .responseCode("400")
                    .responseMessage("Delivery must be marked as PAID before rating.")
                    .deliveryId(delivery.getDeliveryId())
                    .build();
        }

        if (delivery.isUserHasRated()) {
            return UserToDeliveryPartnerResponse.builder()
                    .success(false)
                    .responseCode("409")
                    .responseMessage("User has already rated this delivery.")
                    .deliveryId(delivery.getDeliveryId())
                    .build();
        }

        UserToDeliveryPartnerRequest partnerUserToDeliveryPartnerRequest = UserToDeliveryPartnerRequest.builder()
                .deliveryPartnerId(delivery.getAssignedDeliveryPartnerId())
                .newRating(userToDeliveryPartnerRequest.getNewRating())
                .build();

        delivery.setUserHasRated(true);
        deliveryRepository.save(delivery);

        // Forward to Delivery Partner service via Feign
        return engineToDeliveryPartnerClient.addRating(partnerUserToDeliveryPartnerRequest, authHeader);
    }
}