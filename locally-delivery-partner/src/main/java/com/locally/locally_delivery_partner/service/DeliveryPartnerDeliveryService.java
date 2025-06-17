package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.DeliveryEngineResponse;
import com.locally.locally_delivery_partner.dto.DeliveryStatusRequest;

public interface DeliveryPartnerDeliveryService {
    DeliveryEngineResponse acceptDelivery(DeliveryStatusRequest deliveryStatusRequest);

    DeliveryEngineResponse markDeliveryInTransit(DeliveryStatusRequest deliveryStatusRequest);

    DeliveryEngineResponse markDeliveryDelivered(DeliveryStatusRequest deliveryStatusRequest);

    DeliveryEngineResponse markDeliveryPaid(DeliveryStatusRequest deliveryStatusRequest);

    DeliveryEngineResponse cancelDelivery(DeliveryStatusRequest deliveryStatusRequest);

    DeliveryEngineResponse deliveryFailed(DeliveryStatusRequest deliveryStatusRequest);

    DeliveryEngineResponse getActiveDelivery(Long deliveryPartnerId, String email);
}