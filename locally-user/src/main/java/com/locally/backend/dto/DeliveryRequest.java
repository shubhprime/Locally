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
    private Long senderId;
    private String email;
    private String pickUpAddress;
    private double pickUpLongitude;
    private double pickUpLatitude;
    private String dropOffAddress;
    private double dropOffLongitude;
    private double dropOffLatitude;
    private String packageDetails;
    private String typeOfDelivery;
    private double distanceInMiles;
}