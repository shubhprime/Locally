package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatedDeliveryRequest {
    private String updatedPickUpAddress;
    private String updatedDropOffAddress;
    private String updatedPackageDetails;
    private String updatedTypeOfDelivery;
    private double updatedDistanceInMiles;
}