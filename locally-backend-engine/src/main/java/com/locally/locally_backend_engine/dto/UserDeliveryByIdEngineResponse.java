package com.locally.locally_backend_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeliveryByIdEngineResponse {
    private Long deliveryId;
    private String pickUpAddress;
    private String dropOffAddress;
    private String packageDetails;
    private String typeOfDelivery;
    private String deliveryStatus;
    private double totalFee;
    private double distanceInMiles;
    private boolean isPaid;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime cancelledAt;
}