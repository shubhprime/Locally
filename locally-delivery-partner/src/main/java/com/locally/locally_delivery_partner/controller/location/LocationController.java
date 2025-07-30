package com.locally.locally_delivery_partner.controller.location;

import com.locally.locally_delivery_partner.dto.*;
import com.locally.locally_delivery_partner.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-partner/v1/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping("/update")
    public ResponseEntity<UpdateLocationResponse> updateDeliveryPartnerLocation(@Valid @RequestBody UpdateLocationRequest updateLocationRequest) {

        UpdateLocationResponse updateLocationResponse = locationService.updateDeliveryPartnerLocation(updateLocationRequest);

        return ResponseEntity.ok(updateLocationResponse);
    }

    @PostMapping("/nearest")
    public ResponseEntity<List<NearestDriverResponse>> getNearestDeliveryPartners(@RequestBody NearestDriverRequest nearestDriverRequest) {

        List<NearestDriverResponse> nearestDrivers = locationService.getNearestDeliveryPartners(nearestDriverRequest);

        return ResponseEntity.ok(nearestDrivers);
    }

    @GetMapping("/get-location/{deliveryPartnerId}")
    public DriverLocationResponse getDriverLocation(@PathVariable Long deliveryPartnerId) {

        Point point = locationService.getLastKnownLocation(deliveryPartnerId);

        return DriverLocationResponse.builder()
                .longitude(point.getX()) // Point stores as (x=lon, y=lat)
                .latitude(point.getY())
                .deliveryPartnerId(deliveryPartnerId)
                .build();
    }
}