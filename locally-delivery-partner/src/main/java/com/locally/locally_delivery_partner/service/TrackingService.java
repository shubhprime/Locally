package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.*;

public interface TrackingService {
    GetDeliveryOfferResponse getDeliveryOffer(GetDeliveryOfferRequest getDeliveryOfferRequest);
    TrackingResponse sendOffer(DeliveryOfferRequest deliveryOfferRequest);
    TrackingResponse acceptDelivery(AcceptDeliveryRequest acceptDeliveryRequest);
    CheckAcceptedResponse checkAccepted(CheckAcceptedRequest checkAcceptedRequest);
}