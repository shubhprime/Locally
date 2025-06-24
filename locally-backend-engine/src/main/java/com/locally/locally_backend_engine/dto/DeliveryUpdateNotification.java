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
    private String updatedDropOffAddress;
    private String updatedPackageDetails;
    private String updatedTypeOfDelivery;
    private double updatedDistanceInMiles;
    private double updatedFee;
}