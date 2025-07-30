package com.locally.locally_backend_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryUpdateNotification {
    private Long deliveryId;
    private String updatedPickUpAddress;
    private double pickUpLongitude;
    private double pickUpLatitude;
    private String updatedDropOffAddress;
    private double dropOffLongitude;
    private double dropOffLatitude;
    private String updatedPackageDetails;
    private String updatedTypeOfDelivery;
    private double updatedDistanceInMiles;
    private double updatedFee;
}