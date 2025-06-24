package com.locally.locally_backend_engine.client.EngineToDeliveryPartner;

import com.locally.locally_backend_engine.config.FeignClientNoErrorDecoderConfig;
import com.locally.locally_backend_engine.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "engine-to-delivery-partner-service", url = "${engine.to.delivery.partner.service.url}", configuration = FeignClientNoErrorDecoderConfig.class)
public interface EngineToDeliveryPartnerClient {

    @PostMapping("/api/delivery-partner/v1/rating/from-user/rate")
    UserToDeliveryPartnerResponse addRating(@RequestBody UserToDeliveryPartnerRequest userToDeliveryPartnerRequest,
                                            @RequestHeader("Authorization") String authHeader);

    @PostMapping("/api/delivery-partner/v1/tracking/send-offer")
    TrackingResponse sendDeliveryOffer(@RequestBody DeliveryOfferRequest deliveryOfferRequest);

    @PostMapping("/api/delivery-partner/v1/tracking/check-accepted")
    CheckAcceptedResponse checkIfAccepted(@RequestBody CheckAcceptedRequest checkAcceptedRequest);

    // Update driver location
    @PostMapping("/api/delivery-partner/v1/location/update")
    UpdateLocationResponse updateLocation(@RequestBody UpdateLocationRequest updateLocationRequest);

    // Fetch nearest drivers
    @PostMapping("/api/delivery-partner/v1/location/nearest")
    List<NearestDriverResponse> getNearestDrivers(@RequestBody NearestDriverRequest nearestDriverRequest);
}