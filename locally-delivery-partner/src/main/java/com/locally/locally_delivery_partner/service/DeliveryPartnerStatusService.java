package com.locally.locally_delivery_partner.service;

public interface DeliveryPartnerStatusService {
    public void markAvailable(String email);

    public void markOnDelivery(String email);

    public void markUnavailable(String email);

    public String getCurrentStatus(String email);
}