package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRequest {
    private String pickUpAddress;
    private String dropOffAddress;
    private String packageDetails;
    private String typeOfDelivery;
    private double distanceInMiles;
}