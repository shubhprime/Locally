package com.locally.locally_backend_engine.service;

import com.locally.locally_backend_engine.dto.DeliveryPartnerLocationRequest;
import com.locally.locally_backend_engine.dto.DeliveryPartnerLocationResponse;

public interface LocationBroadcastService {
    void broadcastLocation(DeliveryPartnerLocationRequest deliveryPartnerLocationRequest);

    DeliveryPartnerLocationResponse getLatestLocation(Long deliveryId);
}