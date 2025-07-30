package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.NearestDriverRequest;
import com.locally.locally_delivery_partner.dto.NearestDriverResponse;
import com.locally.locally_delivery_partner.dto.UpdateLocationRequest;
import com.locally.locally_delivery_partner.dto.UpdateLocationResponse;
import org.springframework.data.geo.Point;

import java.util.List;

public interface LocationService {
    UpdateLocationResponse updateDeliveryPartnerLocation(UpdateLocationRequest updateLocationRequest);

    List<NearestDriverResponse> getNearestDeliveryPartners(NearestDriverRequest nearestDriverRequest);

    Point getLastKnownLocation(Long deliveryPartnerId);
}