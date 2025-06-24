package com.locally.locally_backend_engine.controller.delivery;

import com.locally.locally_backend_engine.dto.*;
import com.locally.locally_backend_engine.service.UserDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/engine/v1/user/delivery")
public class UserDeliveryController {

    @Autowired
    private UserDeliveryService userDeliveryService;

    @PostMapping("/create-delivery")
    public ResponseEntity<DeliveryEngineResponse> createDelivery(@RequestBody DeliveryEngineRequest deliveryRequest) {
        DeliveryEngineResponse deliveryEngineResponse = userDeliveryService.createDelivery(deliveryRequest);
        return ResponseEntity.ok(deliveryEngineResponse);
    }

    @GetMapping("/get-all-user-delivery")
    public ResponseEntity<PaginatedDeliveryEngineResponse> getAllDeliveriesForUser(
            @RequestParam Long senderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        PaginatedDeliveryEngineResponse paginatedDeliveryEngineResponse = userDeliveryService.getAllDeliveriesForUser(senderId, page, size);
        return ResponseEntity.ok(paginatedDeliveryEngineResponse);
    }

    @GetMapping("/get-user-delivery-by-id")
    public ResponseEntity<UserDeliveryByIdEngineResponse> getDeliveryByIdForUser(
            @RequestParam Long deliveryId,
            @RequestParam Long senderId
    ) {
        UserDeliveryByIdEngineResponse userDeliveryByIdEngineResponse = userDeliveryService.getDeliveryByIdForUser(senderId, deliveryId);
        return ResponseEntity.ok(userDeliveryByIdEngineResponse);
    }

    @PutMapping("/update-delivery")
    public ResponseEntity<DeliveryEngineResponse> updateDelivery(@RequestBody UpdatedDeliveryEngineRequest updatedDeliveryRequest) {
        DeliveryEngineResponse deliveryEngineResponse = userDeliveryService.updateDelivery(updatedDeliveryRequest);
        return ResponseEntity.ok(deliveryEngineResponse);
    }

    @PutMapping("/cancel-delivery")
    public ResponseEntity<DeliveryEngineResponse> cancelDelivery(@RequestBody CancelDeliveryEngineRequest cancelDeliveryRequest) {
        DeliveryEngineResponse deliveryEngineResponse = userDeliveryService.cancelDelivery(cancelDeliveryRequest);
        return ResponseEntity.ok(deliveryEngineResponse);
    }
}