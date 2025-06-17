package com.locally.locally_backend_engine.controller.rating;

import com.locally.locally_backend_engine.dto.UserToDeliveryPartnerRequest;
import com.locally.locally_backend_engine.dto.UserToDeliveryPartnerResponse;
import com.locally.locally_backend_engine.service.UserRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/engine/v1/user/rating/from-user")
public class UserToDeliveryPartnerRatingController {

    @Autowired
    private UserRatingService userRatingService;

    @PostMapping("/rate-delivery")
    public ResponseEntity<UserToDeliveryPartnerResponse> rateDelivery(@RequestBody UserToDeliveryPartnerRequest userToDeliveryPartnerRequest, @RequestHeader("Authorization") String authHeader) {
        UserToDeliveryPartnerResponse userToDeliveryPartnerResponse = userRatingService.rateDelivery(userToDeliveryPartnerRequest, authHeader);

        return ResponseEntity.ok(userToDeliveryPartnerResponse);
    }
}