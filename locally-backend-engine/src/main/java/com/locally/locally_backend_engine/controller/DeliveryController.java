package com.locally.locally_backend_engine.controller;

import com.locally.locally_backend_engine.dto.*;
import com.locally.locally_backend_engine.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/engine/v1/user/delivery")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @PostMapping("/create-delivery")
    public ResponseEntity<DeliveryEngineResponse> createDelivery(@RequestBody DeliveryEngineRequest deliveryRequest) {
        DeliveryEngineResponse response = deliveryService.createDelivery(deliveryRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all-user-delivery")
    public ResponseEntity<PaginatedDeliveryEngineResponse> getAllDeliveriesForUser(
            @RequestParam Long senderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        PaginatedDeliveryEngineResponse response = deliveryService.getAllDeliveriesForUser(senderId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-user-delivery-by-id")
    public ResponseEntity<UserDeliveryByIdEngineResponse> getDeliveryByIdForUser(
            @RequestParam Long deliveryId,
            @RequestParam Long senderId
    ) {
        UserDeliveryByIdEngineResponse response = deliveryService.getDeliveryByIdForUser(senderId, deliveryId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-delivery")
    public ResponseEntity<DeliveryEngineResponse> updateDelivery(@RequestBody UpdatedDeliveryEngineRequest updatedDeliveryRequest) {
        DeliveryEngineResponse response = deliveryService.updateDelivery(updatedDeliveryRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/cancel-delivery")
    public ResponseEntity<DeliveryEngineResponse> cancelDelivery(@RequestBody CancelDeliveryEngineRequest cancelDeliveryRequest) {
        DeliveryEngineResponse response = deliveryService.cancelDelivery(cancelDeliveryRequest);
        return ResponseEntity.ok(response);
    }
}