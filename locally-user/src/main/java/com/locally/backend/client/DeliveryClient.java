package com.locally.backend.client;

import com.locally.backend.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "delivery-service", url = "${delivery.service.url}")
public interface DeliveryClient {

    @PostMapping("/api/engine/user/delivery/create-delivery")
    DeliveryResponse createDelivery(
            @RequestBody DeliveryRequest deliveryRequest);

    @GetMapping("/api/engine/user/delivery/get-all-user-delivery")
    PaginatedDeliveryResponse getAllDeliveriesForUser(
            @RequestParam Long senderId,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/engine/user/delivery/get-user-delivery-by-id")
    UserDeliveryByIdResponse getDeliveryByIdForUser(
            @RequestParam Long deliveryId,
            @RequestParam Long senderId
    );

    @PutMapping("/api/engine/user/delivery/update-delivery")
    DeliveryResponse updateDelivery(
            @RequestBody UpdatedDeliveryRequest updatedDeliveryRequest
    );

    // TODO: Change to PacthMapping in the future
    @PutMapping("/api/engine/user/delivery/cancel-delivery")
    DeliveryResponse cancelDelivery(
            @RequestBody CancelDeliveryRequest cancelDeliveryRequest
    );
}