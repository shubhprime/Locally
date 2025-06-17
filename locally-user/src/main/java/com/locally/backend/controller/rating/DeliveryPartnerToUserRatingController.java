package com.locally.backend.controller.rating;

import com.locally.backend.dto.DeliveryPartnerToUserRequest;
import com.locally.backend.dto.DeliveryPartnerToUserResponse;
import com.locally.backend.service.UserRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/v1/rating/from-delivery-partner")
public class DeliveryPartnerToUserRatingController {

    @Autowired
    private UserRatingService userRatingService;

    @PostMapping("/rate")
    public ResponseEntity<DeliveryPartnerToUserResponse> addRating(@RequestBody DeliveryPartnerToUserRequest deliveryPartnerToUserRequest, @RequestHeader("Authorization") String authHeader) {
        DeliveryPartnerToUserResponse deliveryPartnerToUserResponse = userRatingService.addRating(deliveryPartnerToUserRequest);

        return ResponseEntity.ok(deliveryPartnerToUserResponse); // 200 OK on success    }
    }
}