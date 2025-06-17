package com.locally.locally_backend_engine.client.EngineToUser;

import com.locally.locally_backend_engine.dto.DeliveryPartnerToUserRatingRequest;
import com.locally.locally_backend_engine.dto.DeliveryPartnerToUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "engine-to-user-service", url = "${engine.to.user.service.url}")
public interface EngineToUserClient {

    @PostMapping("/api/user/v1/rating/from-delivery-partner/rate")
    DeliveryPartnerToUserResponse addRating(@RequestBody DeliveryPartnerToUserRatingRequest deliveryPartnerToUserRatingRequest, @RequestHeader("Authorization") String authHeader);
}