package com.locally.locally_delivery_partner.controller.rating;

import com.locally.locally_delivery_partner.dto.UserToDeliveryPartnerRequest;
import com.locally.locally_delivery_partner.dto.UserToDeliveryPartnerResponse;
import com.locally.locally_delivery_partner.service.DeliveryPartnerRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery-partner/v1/rating/from-user")
public class UserToDeliveryPartnerRatingController {

    @Autowired
    private DeliveryPartnerRatingService deliveryPartnerRatingService;

    @PostMapping("/rate")
    public ResponseEntity<UserToDeliveryPartnerResponse> addRating(@RequestBody UserToDeliveryPartnerRequest userToDeliveryPartnerRequest, @RequestHeader("Authorization") String authHeader) {
        UserToDeliveryPartnerResponse userToDeliveryPartnerResponse = deliveryPartnerRatingService.addRating(userToDeliveryPartnerRequest);

        return ResponseEntity.ok(userToDeliveryPartnerResponse);
    }
}