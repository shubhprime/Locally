package com.locally.locally_delivery_partner.service;

public interface DeliveryPartnerStatusService {
    void markAvailable(String email);

    void markOnDelivery(String email);

    void markUnavailable(String email);

    String getCurrentStatus(String email);
}