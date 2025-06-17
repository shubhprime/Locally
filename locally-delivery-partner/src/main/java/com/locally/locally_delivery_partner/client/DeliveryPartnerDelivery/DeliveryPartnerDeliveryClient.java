package com.locally.locally_delivery_partner.client.DeliveryPartnerDelivery;

import com.locally.locally_delivery_partner.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "delivery-partner-delivery-service", url = "${delivery.partner.delivery.service.url}")
public interface DeliveryPartnerDeliveryClient {

    @PostMapping("/api/engine/v1/delivery-partner/delivery/accept-delivery")
    DeliveryEngineResponse acceptDelivery(
            @RequestBody DeliveryStatusRequest deliveryStatusRequest
    );

    @PostMapping("/api/engine/v1/delivery-partner/delivery/mark-delivery-in-transit")
    DeliveryEngineResponse markDeliveryInTransit(
            @RequestBody DeliveryStatusRequest deliveryStatusRequest
    );

    @PostMapping("/api/engine/v1/delivery-partner/delivery/mark-delivery-delivered")
    DeliveryEngineResponse markDeliveryDelivered(
            @RequestBody DeliveryStatusRequest deliveryStatusRequest
    );

    @PostMapping("/api/engine/v1/delivery-partner/delivery/mark-delivery-paid")
    DeliveryEngineResponse markDeliveryPaid(
            @RequestBody DeliveryStatusRequest deliveryStatusRequest
    );

    @PostMapping("/api/engine/v1/delivery-partner/delivery/cancel-delivery")
    DeliveryEngineResponse cancelDelivery(
            @RequestBody DeliveryStatusRequest deliveryStatusRequest
    );

    @PostMapping("/api/engine/v1/delivery-partner/delivery/delivery-failed")
    DeliveryEngineResponse deliveryFailed(
            @RequestBody DeliveryStatusRequest deliveryStatusRequest
    );

    @GetMapping("/api/engine/v1/delivery-partner/delivery/get-active-delivery/{deliveryPartnerId}")
    DeliveryEngineResponse getActiveDelivery(
            @PathVariable Long deliveryPartnerId
    );
}