package com.locally.locally_backend_engine.service;


import com.locally.locally_backend_engine.dto.TrackingRequest;
import com.locally.locally_backend_engine.dto.TrackingResponse;

public interface TrackingService {
    TrackingResponse assignDelivery(TrackingRequest trackingRequest);
}