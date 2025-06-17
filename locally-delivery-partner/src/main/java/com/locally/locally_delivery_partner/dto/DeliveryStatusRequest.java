package com.locally.locally_delivery_partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryStatusRequest {
    private String email;
    private Long deliveryId;
    private Long deliveryPartnerId;
    private String cancellationReason;
    private String failureReason;
}