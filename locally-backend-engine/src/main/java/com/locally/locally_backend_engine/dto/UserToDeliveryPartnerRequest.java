package com.locally.locally_backend_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserToDeliveryPartnerRequest {
    private Long deliveryId;
    private Long deliveryPartnerId;
    private Double newRating;
}