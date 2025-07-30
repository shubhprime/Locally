package com.locally.locally_backend_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatedDeliveryEngineRequest {
    private Long deliveryId;
    private Long senderId;
    private String updatedPickUpAddress;
    private double pickUpLongitude;
    private double pickUpLatitude;
    private String updatedDropOffAddress;
    private double dropOffLongitude;
    private double dropOffLatitude;
    private String updatedPackageDetails;
    private String updatedTypeOfDelivery;
    private double updatedDistanceInMiles;
    private double updatedLatitude;
    private double updatedLongitude;
}