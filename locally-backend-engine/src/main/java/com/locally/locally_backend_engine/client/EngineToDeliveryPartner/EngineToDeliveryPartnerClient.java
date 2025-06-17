package com.locally.locally_backend_engine.client.EngineToDeliveryPartner;

import com.locally.locally_backend_engine.dto.UserToDeliveryPartnerRequest;
import com.locally.locally_backend_engine.dto.UserToDeliveryPartnerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "engine-to-delivery-partner-service", url = "${engine.to.delivery.partner.service.url}")
public interface EngineToDeliveryPartnerClient {

    @PostMapping("/api/delivery-partner/v1/rating/from-user/rate")
    UserToDeliveryPartnerResponse addRating(@RequestBody UserToDeliveryPartnerRequest userToDeliveryPartnerRequest, @RequestHeader("Authorization") String authHeader);
}