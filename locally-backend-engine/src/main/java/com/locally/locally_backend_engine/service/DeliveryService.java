package com.locally.locally_backend_engine.service;

import com.locally.locally_backend_engine.dto.*;

public interface DeliveryService {
    public DeliveryEngineResponse createDelivery(DeliveryEngineRequest deliveryEngineRequest);

    public PaginatedDeliveryEngineResponse getAllDeliveriesForUser(Long senderId, int page, int size);

    public UserDeliveryByIdEngineResponse getDeliveryByIdForUser(Long senderId, Long deliveryId);

    public DeliveryEngineResponse updateDelivery(UpdatedDeliveryEngineRequest updatedDeliveryEngineRequest);

    public DeliveryEngineResponse cancelDelivery(CancelDeliveryEngineRequest cancelDeliveryEngineRequest);

//    public DeliveryStatusUpdateResponse updateDeliveryStatus(String email, Long id, String newDeliveryStatus);
}