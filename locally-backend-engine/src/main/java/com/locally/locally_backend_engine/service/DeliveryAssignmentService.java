package com.locally.locally_backend_engine.service;

public interface DeliveryAssignmentService {
    void assign(Long deliveryPartnerId, Long deliveryId);

    void markCompleted(Long deliveryId);
}