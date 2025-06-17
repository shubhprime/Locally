package com.locally.locally_delivery_partner.controller.delivery;

import com.locally.locally_delivery_partner.dto.DeliveryEngineResponse;
import com.locally.locally_delivery_partner.dto.DeliveryStatusRequest;
import com.locally.locally_delivery_partner.service.DeliveryPartnerDeliveryService;
import com.locally.locally_delivery_partner.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery-partner/v1/delivery")
public class DeliveryPartnerDeliveryController {

    @Autowired
    private DeliveryPartnerDeliveryService deliveryService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/accept")
    public ResponseEntity<DeliveryEngineResponse> acceptDelivery(@RequestBody DeliveryStatusRequest deliveryStatusRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        deliveryStatusRequest.setEmail(email);

        DeliveryEngineResponse deliveryEngineResponse = deliveryService.acceptDelivery(deliveryStatusRequest);
        return ResponseEntity.ok(deliveryEngineResponse);
    }

    @PostMapping("/in-transit")
    public ResponseEntity<DeliveryEngineResponse> markDeliveryInTransit(@RequestBody DeliveryStatusRequest deliveryStatusRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        deliveryStatusRequest.setEmail(email);

        DeliveryEngineResponse deliveryEngineResponse = deliveryService.markDeliveryInTransit(deliveryStatusRequest);
        return ResponseEntity.ok(deliveryEngineResponse);
    }

    @PostMapping("/delivered")
    public ResponseEntity<DeliveryEngineResponse> markDeliveryDelivered(@RequestBody DeliveryStatusRequest deliveryStatusRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        deliveryStatusRequest.setEmail(email);

        DeliveryEngineResponse deliveryEngineResponse = deliveryService.markDeliveryDelivered(deliveryStatusRequest);
        return ResponseEntity.ok(deliveryEngineResponse);
    }

    @PostMapping("/paid")
    public ResponseEntity<DeliveryEngineResponse> markDeliveryPaid(@RequestBody DeliveryStatusRequest deliveryStatusRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        deliveryStatusRequest.setEmail(email);

        DeliveryEngineResponse deliveryEngineResponse = deliveryService.markDeliveryPaid(deliveryStatusRequest);
        return ResponseEntity.ok(deliveryEngineResponse);
    }

    @PostMapping("/cancel")
    public ResponseEntity<DeliveryEngineResponse> cancelDelivery(@RequestBody DeliveryStatusRequest deliveryStatusRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        deliveryStatusRequest.setEmail(email);

        DeliveryEngineResponse deliveryEngineResponse = deliveryService.cancelDelivery(deliveryStatusRequest);
        return ResponseEntity.ok(deliveryEngineResponse);
    }

    @PostMapping("/failed")
    public ResponseEntity<DeliveryEngineResponse> deliveryFailed(@RequestBody DeliveryStatusRequest deliveryStatusRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        deliveryStatusRequest.setEmail(email);

        DeliveryEngineResponse deliveryEngineResponse = deliveryService.deliveryFailed(deliveryStatusRequest);
        return ResponseEntity.ok(deliveryEngineResponse);
    }

    @GetMapping("/active/{deliveryPartnerId}")
    public ResponseEntity<DeliveryEngineResponse> getActiveDelivery(@PathVariable Long deliveryPartnerId, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        DeliveryEngineResponse deliveryEngineResponse = deliveryService.getActiveDelivery(deliveryPartnerId, email);
        return ResponseEntity.ok(deliveryEngineResponse);
    }
}