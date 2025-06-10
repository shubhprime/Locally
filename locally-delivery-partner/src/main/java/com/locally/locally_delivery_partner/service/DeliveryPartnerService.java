package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.*;

public interface DeliveryPartnerService {
    public DeliveryPartnerResponse createDeliveryPartner(CreateDeliveryPartnerRequest createDeliveryPartnerRequest);

    public DeliveryPartnerResponse createVehicle(CreateVehicleRequest createVehicleRequest);

    public DeliveryPartnerResponse loginDeliveryPartner(LoginRequest loginRequest);

    public DeliveryPartnerResponse logoutDeliveryPartner(RefreshTokenRequest logoutRequest);
}