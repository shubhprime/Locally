package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.*;

import java.util.List;

public interface VehicleService {
    public DeliveryPartnerResponse createVehicle(CreateVehicleRequest createVehicleRequest);

    public List<VehicleResponse> listVehicles(VehicleListRequest vehicleListRequest);

    public DeliveryPartnerResponse updateVehicle(UpdateVehicleRequest updateVehicleRequest);

    public DeliveryPartnerResponse deleteVehicle(Long vehicleId, String email);
}