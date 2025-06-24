package com.locally.locally_delivery_partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckAcceptedResponse {
    private String responseCode;
    private boolean success;
    private String responseMessage;
    private Long deliveryId;
    private Long deliveryPartnerId;
}