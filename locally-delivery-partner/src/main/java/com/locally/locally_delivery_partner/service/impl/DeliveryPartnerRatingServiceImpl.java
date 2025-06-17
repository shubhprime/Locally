package com.locally.locally_delivery_partner.service.impl;

import com.locally.locally_delivery_partner.dto.UserToDeliveryPartnerRequest;
import com.locally.locally_delivery_partner.dto.UserToDeliveryPartnerResponse;
import com.locally.locally_delivery_partner.model.DeliveryPartner;
import com.locally.locally_delivery_partner.repository.DeliveryPartnerRepository;
import com.locally.locally_delivery_partner.service.DeliveryPartnerRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryPartnerRatingServiceImpl implements DeliveryPartnerRatingService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Transactional
    public UserToDeliveryPartnerResponse addRating(UserToDeliveryPartnerRequest userToDeliveryPartnerRequest) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(userToDeliveryPartnerRequest.getDeliveryPartnerId())
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        double currentTotalScore = deliveryPartner.getAverageRating() * deliveryPartner.getTotalRatings();
        int newTotalRatings = deliveryPartner.getTotalRatings() + 1;

        double newAverage = (currentTotalScore + userToDeliveryPartnerRequest.getNewRating()) / newTotalRatings;

        deliveryPartner.setTotalRatings(newTotalRatings);
        deliveryPartner.setAverageRating(newAverage);

        deliveryPartnerRepository.save(deliveryPartner);
        return UserToDeliveryPartnerResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Rating submitted successfully")
                .build();
    }
}