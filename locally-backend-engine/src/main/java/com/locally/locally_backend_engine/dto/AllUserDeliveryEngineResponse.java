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
public class AllUserDeliveryEngineResponse {
    private Long deliveryId;
    private String pickUpAddress;
    private String dropOffAddress;
    private String packageDetails;
    private String typeOfDelivery;
    private String deliveryStatus;
    private double totalFee;
    private LocalDateTime createdAt;
}