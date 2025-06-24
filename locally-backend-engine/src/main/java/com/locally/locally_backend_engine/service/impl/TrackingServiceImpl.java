package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.client.EngineToDeliveryPartner.EngineToDeliveryPartnerClient;
import com.locally.locally_backend_engine.dto.*;
import com.locally.locally_backend_engine.service.DeliveryAssignmentService;
import com.locally.locally_backend_engine.service.DeliveryPartnerDeliveryService;
import com.locally.locally_backend_engine.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackingServiceImpl implements TrackingService {

    @Autowired
    private EngineToDeliveryPartnerClient engineToDeliveryPartnerClient;

    @Autowired
    private DeliveryAssignmentService deliveryAssignmentService;

    @Autowired
    private DeliveryPartnerDeliveryService deliveryPartnerDeliveryService;

    @Override
    public TrackingResponse assignDelivery(TrackingRequest trackingRequest) {

        List<NearestDriverResponse> nearbyDriverIds;

        // Step 1: Call delivery-partner to get nearby drivers
        try {
            nearbyDriverIds = engineToDeliveryPartnerClient.getNearestDrivers(
                    NearestDriverRequest.builder()
                            .latitude(trackingRequest.getLatitude())
                            .longitude(trackingRequest.getLongitude())
                            .radiusMile(5.0)
                            .limit(8)
                            .build()
            );
        } catch (Exception e) {
            return TrackingResponse.builder()
                    .responseCode("500")
                    .success(false)
                    .responseMessage("Error fetching nearest drivers: " + e.getMessage())
                    .deliveryId(trackingRequest.getDeliveryId())
                    .build();
        }

        if (nearbyDriverIds == null || nearbyDriverIds.isEmpty()) {
            return TrackingResponse.builder()
                    .responseCode("404")
                    .success(false)
                    .responseMessage("No nearby drivers found")
                    .deliveryId(trackingRequest.getDeliveryId())
                    .build();
        }

        for (NearestDriverResponse driver : nearbyDriverIds) {
            Long deliveryPartnerId = driver.getDeliveryPartnerId();

            // Step 2: Send offer to delivery-partner backend (store in Redis)
            try {
                engineToDeliveryPartnerClient.sendDeliveryOffer(
                        DeliveryOfferRequest.builder()
                                .deliveryId(trackingRequest.getDeliveryId())
                                .deliveryPartnerId(deliveryPartnerId)
                                .build());
            } catch (Exception e) {
                continue;
            }

            // Step 3: Wait for acceptance
            for (int i = 0; i < 20; i++) {
                System.out.println("Waiting for driver to accept... Attempt " + i);

                CheckAcceptedResponse checkAcceptedResponse = null;
                try {
                    checkAcceptedResponse = engineToDeliveryPartnerClient.checkIfAccepted(
                            CheckAcceptedRequest.builder()
                                    .deliveryId(trackingRequest.getDeliveryId())
                                    .build()
                    );
                } catch (Exception e) {
                    System.out.println("Attempt " + i + ": Error checking acceptance: " + e.getMessage());
//                    continue;
                }

                if (checkAcceptedResponse != null) {
                    Long acceptedDeliveryPartnerId = checkAcceptedResponse.getDeliveryPartnerId();

                    if (Boolean.TRUE.equals(checkAcceptedResponse.isSuccess())
                            && deliveryPartnerId.equals(acceptedDeliveryPartnerId)) {

                        deliveryPartnerId = acceptedDeliveryPartnerId;

                        // Step 4: Assign delivery
                        try {
                            deliveryAssignmentService.assign(deliveryPartnerId, trackingRequest.getDeliveryId());

                            DeliveryStatusRequest acceptDeliveryRequest = DeliveryStatusRequest.builder()
                                    .deliveryId(trackingRequest.getDeliveryId())
                                    .deliveryPartnerId(deliveryPartnerId)
                                    .cancellationReason(null)
                                    .failureReason(null)
                                    .build();

                            DeliveryEngineResponse engineResponse = deliveryPartnerDeliveryService.acceptDelivery(acceptDeliveryRequest);

                            if (!engineResponse.isSuccess()) {
                                return TrackingResponse.builder()
                                        .responseCode("500")
                                        .success(false)
                                        .responseMessage("Failed to update delivery status: " + engineResponse.getResponseMessage())
                                        .deliveryId(trackingRequest.getDeliveryId())
                                        .build();
                            }

                        } catch (Exception e) {
                            return TrackingResponse.builder()
                                    .responseCode("500")
                                    .success(false)
                                    .responseMessage("Failed to assign delivery: " + e.getMessage())
                                    .deliveryId(trackingRequest.getDeliveryId())
                                    .build();
                        }

                        return TrackingResponse.builder()
                                .responseCode("200")
                                .success(true)
                                .responseMessage("Assigned to driver: " + deliveryPartnerId)
                                .deliveryId(trackingRequest.getDeliveryId())
                                .deliveryPartnerId(deliveryPartnerId)
                                .build();
                    }
                }

                try {
                    Thread.sleep(1000); // 1 second pause
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // good practice
                    return TrackingResponse.builder()
                            .responseCode("500")
                            .success(false)
                            .responseMessage("Interrupted while waiting for driver acceptance")
                            .deliveryId(trackingRequest.getDeliveryId())
                            .build();
                }
            }
        }

        // No driver accepted
        return TrackingResponse.builder()
                .responseCode("404")
                .success(false)
                .responseMessage("No driver accepted the delivery")
                .deliveryId(trackingRequest.getDeliveryId())
                .build();
    }
}