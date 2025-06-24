package com.locally.locally_backend_engine.controller.tracking;

import com.locally.locally_backend_engine.dto.TrackingRequest;
import com.locally.locally_backend_engine.dto.TrackingResponse;
import com.locally.locally_backend_engine.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/engine/v1/tracking")
public class TrackingController {

    @Autowired
    private TrackingService trackingService;

    @PostMapping("/assign-delivery")
    public ResponseEntity<TrackingResponse> assignDelivery(@RequestBody TrackingRequest trackingRequest) {
        TrackingResponse trackingResponse = trackingService.assignDelivery(trackingRequest);

        if (trackingResponse.isSuccess()) {
            return ResponseEntity.ok(trackingResponse);
        } else if ("500".equals(trackingResponse.getResponseCode())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(trackingResponse);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(trackingResponse);
        }
    }
}