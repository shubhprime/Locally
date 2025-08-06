package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.*;

import java.util.List;

public interface VehicleService {
    AppResponse<Void> createVehicle(CreateVehicleRequest createVehicleRequest);

    List<VehicleResponse> listVehicles(VehicleListRequest vehicleListRequest);

    AppResponse<Void> updateVehicle(UpdateVehicleRequest updateVehicleRequest);

    AppResponse<Void> deleteVehicle(Long vehicleId, String email);
}