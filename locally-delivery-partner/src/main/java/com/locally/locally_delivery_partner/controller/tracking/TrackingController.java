package com.locally.locally_delivery_partner.controller.tracking;

import com.locally.locally_delivery_partner.dto.*;
import com.locally.locally_delivery_partner.service.TrackingService;
import com.locally.locally_delivery_partner.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery-partner/v1/tracking")
public class TrackingController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TrackingService trackingService;

    @PostMapping("/get-delivery-offer")
    public ResponseEntity<GetDeliveryOfferResponse> getDeliveryOffer(@RequestBody GetDeliveryOfferRequest getDeliveryOfferRequest) {

        GetDeliveryOfferResponse getDeliveryOfferResponse = trackingService.getDeliveryOffer(getDeliveryOfferRequest);

        return ResponseEntity.status(Integer.parseInt(getDeliveryOfferResponse.getResponseCode())).body(getDeliveryOfferResponse);
    }

    @PostMapping("/send-offer")
    public ResponseEntity<TrackingResponse> sendOffer(@RequestBody DeliveryOfferRequest deliveryOfferRequest) {

        TrackingResponse trackingResponse = trackingService.sendOffer(deliveryOfferRequest);

        return ResponseEntity.status(Integer.parseInt(trackingResponse.getResponseCode())).body(trackingResponse);
    }

    // Endpoint called by driver app to accept
    @PostMapping("/accept-delivery")
    public ResponseEntity<TrackingResponse> acceptDelivery(@RequestBody AcceptDeliveryRequest acceptDeliveryRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        acceptDeliveryRequest.setEmail(email);

        TrackingResponse trackingResponse = trackingService.acceptDelivery(acceptDeliveryRequest);

        return ResponseEntity.status(Integer.parseInt(trackingResponse.getResponseCode())).body(trackingResponse);
    }

    @PostMapping("/check-accepted")
    public ResponseEntity<CheckAcceptedResponse> checkAccepted(@RequestBody CheckAcceptedRequest checkAcceptedRequest) {

        CheckAcceptedResponse checkAcceptedResponse = trackingService.checkAccepted(checkAcceptedRequest);

        return ResponseEntity.status(Integer.parseInt(checkAcceptedResponse.getResponseCode())).body(checkAcceptedResponse);
    }
}