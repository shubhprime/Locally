package com.locally.backend.controller.delivery;

import com.locally.backend.dto.*;
import com.locally.backend.service.UserDeliveryService;
import com.locally.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/v1/delivery")
public class UserDeliveryController {

    @Autowired
    private UserDeliveryService userDeliveryService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create-delivery")
    public ResponseEntity<DeliveryResponse> createDelivery(@RequestBody DeliveryRequest deliveryRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        deliveryRequest.setEmail(email);

        DeliveryResponse deliveryResponse = userDeliveryService.createDelivery(deliveryRequest);
        return ResponseEntity.ok(deliveryResponse);
    }

    @GetMapping("/get-all-user-delivery")
    public ResponseEntity<PaginatedDeliveryResponse> getAllUserDeliveries(@RequestHeader("Authorization") String authHeader, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        PaginatedDeliveryResponse deliveries = userDeliveryService.getAllDeliveriesForUser(email, page, size);
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/get-user-delivery-by-id")
    public ResponseEntity<UserDeliveryByIdResponse> getDeliveryByIdForUser(@RequestHeader("Authorization") String authHeader, @RequestParam Long deliveryId) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        UserDeliveryByIdResponse userDeliveryByIdResponse = userDeliveryService.getDeliveryByIdForUser(email, deliveryId);
        return ResponseEntity.ok(userDeliveryByIdResponse);
    }

    @PutMapping("/update-delivery/{deliveryId}")
    public ResponseEntity<DeliveryResponse> updateDelivery(@PathVariable Long deliveryId, @RequestBody UpdatedDeliveryRequest updatedDeliveryRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        DeliveryResponse deliveryResponse = userDeliveryService.updateDelivery(deliveryId, updatedDeliveryRequest, email);
        return ResponseEntity.ok(deliveryResponse);
    }

    @PutMapping("/cancel-delivery/{deliveryId}")
    public ResponseEntity<DeliveryResponse> cancelDelivery(@PathVariable Long deliveryId, @RequestBody CancelDeliveryRequest cancelDeliveryRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        DeliveryResponse deliveryResponse = userDeliveryService.cancelDelivery(deliveryId, cancelDeliveryRequest, email);
        return ResponseEntity.ok(deliveryResponse);
    }
}