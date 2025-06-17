package com.locally.locally_delivery_partner.controller.rating;

import com.locally.locally_delivery_partner.dto.DeliveryPartnerToUserRatingRequest;
import com.locally.locally_delivery_partner.dto.DeliveryPartnerToUserResponse;
import com.locally.locally_delivery_partner.service.UserRatingService;
import com.locally.locally_delivery_partner.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery-partner/v1/rating/from-delivery-partner")
public class DeliveryPartnerToUserRatingController {

    @Autowired
    private UserRatingService userRatingService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/rate")
    public ResponseEntity<DeliveryPartnerToUserResponse> addRating(@RequestBody DeliveryPartnerToUserRatingRequest deliveryPartnerToUserRatingRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        deliveryPartnerToUserRatingRequest.setEmail(email);

        DeliveryPartnerToUserResponse userToDeliveryPartnerResponse = userRatingService.addRating(deliveryPartnerToUserRatingRequest, authHeader);

        if (!userToDeliveryPartnerResponse.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(userToDeliveryPartnerResponse);
        }

        return ResponseEntity.ok(userToDeliveryPartnerResponse);
    }
}