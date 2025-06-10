package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllUserDeliveryResponse {
    private Long id;
    private String pickUpAddress;
    private String dropOffAddress;
    private String packageDetails;
    private String typeOfDelivery;
    private String deliveryStatus;
    private double totalFee;
    private LocalDateTime createdAt;
}