package com.locally.locally_delivery_partner.client.DeliveryPartnerToEngine;

import com.locally.locally_delivery_partner.dto.DeliveryPartnerToUserRatingRequest;
import com.locally.locally_delivery_partner.dto.DeliveryPartnerToUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "delivery-partner-to-engine-service", url = "${delivery.partner.to.engine.service.url}")
public interface DeliveryPartnerToEngineClient {

    @PostMapping("/api/engine/v1/delivery-partner/rating/from-delivery-partner/rate-delivery")
    DeliveryPartnerToUserResponse rateDelivery(@RequestBody DeliveryPartnerToUserRatingRequest deliveryPartnerToUserRatingRequest, @RequestHeader("Authorization") String authHeader);
}