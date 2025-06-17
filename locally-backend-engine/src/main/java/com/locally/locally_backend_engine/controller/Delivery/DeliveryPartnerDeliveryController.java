package com.locally.locally_backend_engine.controller.Delivery;

import com.locally.locally_backend_engine.dto.*;
import com.locally.locally_backend_engine.service.DeliveryPartnerDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/engine/v1/delivery-partner/delivery")
public class DeliveryPartnerDeliveryController {

    @Autowired
    private DeliveryPartnerDeliveryService deliveryPartnerDeliveryService;

    @PostMapping("/accept-delivery")
    public ResponseEntity<DeliveryEngineResponse> acceptDelivery(@RequestBody DeliveryStatusRequest deliveryStatusRequest) {
        DeliveryEngineResponse deliveryEngineResponse = deliveryPartnerDeliveryService.acceptDelivery(deliveryStatusRequest);
        return ResponseEntity.status(deliveryEngineResponse.isSuccess() ? 200 : 400).body(deliveryEngineResponse);
    }

    @PostMapping("/mark-delivery-in-transit")
    public ResponseEntity<DeliveryEngineResponse> markDeliveryInTransit(@RequestBody DeliveryStatusRequest deliveryStatusRequest) {
        DeliveryEngineResponse deliveryEngineResponse = deliveryPartnerDeliveryService.markDeliveryInTransit(deliveryStatusRequest);
        return ResponseEntity.status(deliveryEngineResponse.isSuccess() ? 200 : 400).body(deliveryEngineResponse);
    }

    @PostMapping("/mark-delivery-delivered")
    public ResponseEntity<DeliveryEngineResponse> markDeliveryDelivered(@RequestBody DeliveryStatusRequest deliveryStatusRequest) {
        DeliveryEngineResponse deliveryEngineResponse = deliveryPartnerDeliveryService.markDeliveryDelivered(deliveryStatusRequest);
        return ResponseEntity.status(deliveryEngineResponse.isSuccess() ? 200 : 400).body(deliveryEngineResponse);
    }

    @PostMapping("/mark-delivery-paid")
    public ResponseEntity<DeliveryEngineResponse> markDeliveryPaid(@RequestBody DeliveryStatusRequest deliveryStatusRequest) {
        DeliveryEngineResponse deliveryEngineResponse = deliveryPartnerDeliveryService.markDeliveryPaid(deliveryStatusRequest);
        return ResponseEntity.status(deliveryEngineResponse.isSuccess() ? 200 : 400).body(deliveryEngineResponse);
    }

    @PostMapping("/cancel-delivery")
    public ResponseEntity<DeliveryEngineResponse> cancelDelivery(@RequestBody DeliveryStatusRequest deliveryStatusRequest) {
        DeliveryEngineResponse deliveryEngineResponse = deliveryPartnerDeliveryService.cancelDelivery(deliveryStatusRequest);
        return ResponseEntity.status(deliveryEngineResponse.isSuccess() ? 200 : 400).body(deliveryEngineResponse);
    }

    @PostMapping("/delivery-failed")
    public ResponseEntity<DeliveryEngineResponse> deliveryFailed(@RequestBody DeliveryStatusRequest deliveryStatusRequest) {
        DeliveryEngineResponse deliveryEngineResponse = deliveryPartnerDeliveryService.markDeliveryFailed(deliveryStatusRequest);
        return ResponseEntity.status(deliveryEngineResponse.isSuccess() ? 200 : 400).body(deliveryEngineResponse);
    }

    @GetMapping("/get-active-delivery/{deliveryPartnerId}")
    public ResponseEntity<DeliveryEngineResponse> getActiveDelivery(@PathVariable Long deliveryPartnerId) {
        DeliveryEngineResponse deliveryEngineResponse = deliveryPartnerDeliveryService.getActiveDeliveryForPartner(deliveryPartnerId);
        return ResponseEntity.status(deliveryEngineResponse.isSuccess() ? 200 : 404).body(deliveryEngineResponse);
    }
}