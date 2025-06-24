package com.locally.locally_backend_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartnerLocationPayload {
    private Long deliveryId;
    private Long deliveryPartnerId;
    private double latitude;
    private double longitude;
    private String phase; // "TO_PICKUP" or "TO_DROPOFF"
}