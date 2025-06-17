package com.locally.backend.client.UserToEngine;

import com.locally.backend.dto.UserToDeliveryPartnerRequest;
import com.locally.backend.dto.UserToDeliveryPartnerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-to-engine-service", url = "${user.to.engine.service.url}")
public interface UserToEngineClient {

    @PostMapping("/api/engine/v1/user/rating/from-user/rate-delivery")
    UserToDeliveryPartnerResponse rateDelivery(@RequestBody UserToDeliveryPartnerRequest userToDeliveryPartnerRequest, @RequestHeader("Authorization") String authHeader);
}