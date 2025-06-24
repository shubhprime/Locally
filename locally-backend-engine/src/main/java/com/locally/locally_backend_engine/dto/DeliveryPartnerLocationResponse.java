package com.locally.locally_backend_engine.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartnerLocationResponse {
    private Long deliveryId;
    private Long deliveryPartnerId;
    @DecimalMin("-180.0") @DecimalMax("180.0")
    private double longitude;
    @DecimalMin("-90.0") @DecimalMax("90.0")
    private double latitude;
    private String phase;
}