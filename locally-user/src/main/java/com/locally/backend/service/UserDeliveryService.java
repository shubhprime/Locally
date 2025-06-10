package com.locally.backend.service;

import com.locally.backend.dto.*;

public interface UserDeliveryService {
    public DeliveryResponse createDelivery(DeliveryRequest deliveryRequest, String email);

    public PaginatedDeliveryResponse getAllDeliveriesForUser(String email, int page, int size);

    public UserDeliveryByIdResponse getDeliveryByIdForUser(String email, Long deliveryId);

    public DeliveryResponse updateDelivery(Long deliveryId, UpdatedDeliveryRequest updatedDeliveryRequest, String email);

    public DeliveryResponse cancelDelivery(Long deliveryId, CancelDeliveryRequest cancelDeliveryRequest, String email);
}