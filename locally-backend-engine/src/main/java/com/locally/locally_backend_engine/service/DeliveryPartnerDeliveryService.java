package com.locally.locally_backend_engine.service;

import com.locally.locally_backend_engine.dto.*;
import org.springframework.stereotype.Service;

@Service
public interface DeliveryPartnerDeliveryService {
    public DeliveryEngineResponse acceptDelivery(DeliveryStatusRequest deliveryStatusRequest);

    public DeliveryEngineResponse markDeliveryInTransit(DeliveryStatusRequest deliveryStatusRequest);

    public DeliveryEngineResponse markDeliveryDelivered(DeliveryStatusRequest deliveryStatusRequest);

    public DeliveryEngineResponse markDeliveryPaid(DeliveryStatusRequest deliveryStatusRequest);

    public DeliveryEngineResponse cancelDelivery(DeliveryStatusRequest deliveryStatusRequest);

    public DeliveryEngineResponse markDeliveryFailed(DeliveryStatusRequest deliveryStatusRequest);

    public DeliveryEngineResponse getActiveDeliveryForPartner(Long deliveryPartnerId);
}