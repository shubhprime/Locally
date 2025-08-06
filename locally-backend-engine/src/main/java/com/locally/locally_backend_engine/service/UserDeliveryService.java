package com.locally.locally_backend_engine.service;

import com.locally.locally_backend_engine.dto.*;

public interface UserDeliveryService {
    DeliveryEngineResponse createDelivery(DeliveryEngineRequest deliveryEngineRequest);

    PaginatedDeliveryEngineResponse getAllDeliveriesForUser(Long senderId, int page, int size);

    UserDeliveryByIdEngineResponse getDeliveryByIdForUser(Long senderId, Long deliveryId);

    DeliveryEngineResponse updateDelivery(UpdatedDeliveryEngineRequest updatedDeliveryEngineRequest);

    DeliveryEngineResponse cancelDelivery(CancelDeliveryEngineRequest cancelDeliveryEngineRequest);

//    public DeliveryStatusUpdateResponse updateDeliveryStatus(String email, Long id, String newDeliveryStatus);
}