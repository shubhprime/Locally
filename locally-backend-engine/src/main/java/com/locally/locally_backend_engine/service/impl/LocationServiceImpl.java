package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.client.EngineToDeliveryPartner.EngineToDeliveryPartnerClient;
import com.locally.locally_backend_engine.dto.NearestDriverRequest;
import com.locally.locally_backend_engine.dto.UpdateLocationRequest;
import com.locally.locally_backend_engine.dto.UpdateLocationResponse;
import com.locally.locally_backend_engine.dto.NearestDriverResponse;
import com.locally.locally_backend_engine.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private EngineToDeliveryPartnerClient deliveryPartnerClient;

    @Override
    public UpdateLocationResponse updateLocation(UpdateLocationRequest updateLocationRequest) {
        return deliveryPartnerClient.updateLocation(updateLocationRequest);
    }

    @Override
    public List<NearestDriverResponse> getNearestDrivers(NearestDriverRequest nearestDriverRequest) {
        return deliveryPartnerClient.getNearestDrivers(nearestDriverRequest);
    }
}