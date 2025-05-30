package com.locally.backend.service;

import com.locally.backend.dto.DeliveryRequest;
import com.locally.backend.dto.DeliveryResponse;
import com.locally.backend.dto.GetUserDeliveryResponse;
import com.locally.backend.dto.UpdatedDeliveryRequest;

import java.util.List;

public interface DeliveryService {
    public DeliveryResponse createDelivery(DeliveryRequest deliveryRequest, String email);

    public List<GetUserDeliveryResponse> getAllDeliveriesForUser(String email);

    public DeliveryResponse updateDelivery(Long id, UpdatedDeliveryRequest updatedDeliveryRequest, String email);

    public DeliveryResponse cancelDelivery(Long id, String email);
}