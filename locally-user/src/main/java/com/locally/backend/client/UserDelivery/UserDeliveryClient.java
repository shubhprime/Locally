package com.locally.backend.client.UserDelivery;

import com.locally.backend.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-delivery-service", url = "${user.delivery.service.url}")
public interface UserDeliveryClient {

    @PostMapping("/api/engine/v1/user/delivery/create-delivery")
    DeliveryResponse createDelivery(
            @RequestBody DeliveryRequest deliveryRequest
    );

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

    // TODO: Change to PatchMapping in the future
    @PutMapping("/api/engine/user/delivery/cancel-delivery")
    DeliveryResponse cancelDelivery(
            @RequestBody CancelDeliveryRequest cancelDeliveryRequest
    );
}