package com.locally.locally_backend_engine.controller.rating;

import com.locally.locally_backend_engine.dto.DeliveryPartnerToUserRatingRequest;
import com.locally.locally_backend_engine.dto.DeliveryPartnerToUserResponse;
import com.locally.locally_backend_engine.service.DeliveryPartnerRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/engine/v1/delivery-partner/rating/from-delivery-partner")
public class DeliveryPartnerToUserRatingController {

    @Autowired
    private DeliveryPartnerRatingService deliveryPartnerRatingService;

    @PostMapping("/rate-delivery")
    public ResponseEntity<DeliveryPartnerToUserResponse> rateDelivery(@RequestBody DeliveryPartnerToUserRatingRequest deliveryPartnerToUserRatingRequest, @RequestHeader("Authorization") String authHeader) {
        DeliveryPartnerToUserResponse deliveryPartnerToUserResponse = deliveryPartnerRatingService.rateDelivery(deliveryPartnerToUserRatingRequest, authHeader);

        return ResponseEntity.ok(deliveryPartnerToUserResponse);
    }
}