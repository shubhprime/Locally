package com.locally.locally_backend_engine.service;

public interface FeeCalculationService {
    double calculateFare(double distanceInMiles, String typeOfDelivery);

    double calculateDeliveryPartnerFare(double totalFare, String typeOfDelivery);
}