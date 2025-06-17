package com.locally.backend.controller.rating;

import com.locally.backend.dto.UserToDeliveryPartnerRequest;
import com.locally.backend.dto.UserToDeliveryPartnerResponse;
import com.locally.backend.service.DeliveryPartnerRatingService;
import com.locally.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/v1/rating/from-user")
public class UserToDeliveryPartnerRatingController {

    @Autowired
    private DeliveryPartnerRatingService deliveryPartnerRatingService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/rate")
    public ResponseEntity<UserToDeliveryPartnerResponse> addRating(@RequestBody UserToDeliveryPartnerRequest userToDeliveryPartnerRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        userToDeliveryPartnerRequest.setEmail(email);

        UserToDeliveryPartnerResponse userToDeliveryPartnerResponse = deliveryPartnerRatingService.addRating(userToDeliveryPartnerRequest, authHeader);

        if (!userToDeliveryPartnerResponse.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(userToDeliveryPartnerResponse);
        }

        return ResponseEntity.ok(userToDeliveryPartnerResponse);
    }
}