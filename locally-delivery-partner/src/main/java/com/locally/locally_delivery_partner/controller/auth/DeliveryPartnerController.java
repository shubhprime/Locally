package com.locally.locally_delivery_partner.controller.auth;

import com.locally.locally_delivery_partner.dto.*;
import com.locally.locally_delivery_partner.service.DeliveryPartnerService;
import com.locally.locally_delivery_partner.utils.DeliveryPartnerUtils;
import com.locally.locally_delivery_partner.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery-partner/v1/auth")
public class DeliveryPartnerController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DeliveryPartnerService deliveryPartnerService;

    @PostMapping("/signup")
    public ResponseEntity<DeliveryPartnerResponse> createUser(@RequestBody CreateDeliveryPartnerRequest createDeliveryPartnerRequest) {
        DeliveryPartnerResponse deliveryPartnerResponse = deliveryPartnerService.createDeliveryPartner(createDeliveryPartnerRequest);

        if (deliveryPartnerResponse.getResponseCode().equals(DeliveryPartnerUtils.ACCOUNT_EXISTS_CODE)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(deliveryPartnerResponse); // 409 Conflict if account exists
        }

        if (deliveryPartnerResponse.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(deliveryPartnerResponse); // 201 Created
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(deliveryPartnerResponse); // Generic fallback
        }
    }

    @PostMapping("/create-vehicle")
    public ResponseEntity<DeliveryPartnerResponse> createVehicle(@RequestBody CreateVehicleRequest createVehicleRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        createVehicleRequest.setEmail(email);

        DeliveryPartnerResponse deliveryPartnerResponse = deliveryPartnerService.createVehicle(createVehicleRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryPartnerResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<DeliveryPartnerResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        DeliveryPartnerResponse deliveryPartnerResponse = deliveryPartnerService.loginDeliveryPartner(loginRequest);

        if (deliveryPartnerResponse.isSuccess()) {
            return ResponseEntity.ok(deliveryPartnerResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(deliveryPartnerResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<DeliveryPartnerResponse> logoutUser(@RequestBody RefreshTokenRequest userRequest) {
        DeliveryPartnerResponse deliveryPartnerResponse = deliveryPartnerService.logoutDeliveryPartner(userRequest);

        if (deliveryPartnerResponse.isSuccess()) {
            return ResponseEntity.ok(deliveryPartnerResponse);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(deliveryPartnerResponse);
        }
    }

    // TODO: Remove this route (ONLY FOR TESTING JWT AUTHORIZATION)
    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "This is a protected resource. You are authenticated!";
    }
}