package com.locally.locally_backend_engine.service;

import com.locally.locally_backend_engine.dto.NearestDriverRequest;
import com.locally.locally_backend_engine.dto.UpdateLocationRequest;
import com.locally.locally_backend_engine.dto.UpdateLocationResponse;
import com.locally.locally_backend_engine.dto.NearestDriverResponse;

import java.util.List;

public interface LocationService {
    UpdateLocationResponse updateLocation(UpdateLocationRequest updateLocationRequest);

    List<NearestDriverResponse> getNearestDrivers(NearestDriverRequest nearestDriverRequest);
}