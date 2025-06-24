package com.locally.locally_backend_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearestDriverResponse {
    private String responseCode;
    private boolean success;
    private String responseMessage;
    private Long deliveryPartnerId;
    private double distanceInMiles;
}