package com.locally.locally_backend_engine.controller.webSocket;

import com.locally.locally_backend_engine.dto.DeliveryPartnerLocationRequest;
import com.locally.locally_backend_engine.dto.DeliveryPartnerLocationResponse;
import com.locally.locally_backend_engine.service.LocationBroadcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/engine/v1/websocket")
public class LocationController {

    @Autowired
    private LocationBroadcastService locationBroadcastService;

    @PostMapping("/location-update")
    public void receiveLocation(@RequestBody DeliveryPartnerLocationRequest deliveryPartnerLocationRequest) {
        locationBroadcastService.broadcastLocation(deliveryPartnerLocationRequest);
    }

    @GetMapping("/latest-location/{deliveryId}")
    public DeliveryPartnerLocationResponse getLatestLocation(@PathVariable Long deliveryId) {
        return locationBroadcastService.getLatestLocation(deliveryId);
    }
}