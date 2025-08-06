package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.service.FeeCalculationService;
import com.locally.locally_backend_engine.utils.EngineUtils;
import org.springframework.stereotype.Service;

@Service
public class FeeCalculationServiceImpl implements FeeCalculationService {

    @Override
    public double calculateFare(double distanceInMiles, String typeOfDelivery) {
        double ratePerMile;
        switch (typeOfDelivery.toUpperCase()) {
            case EngineUtils.TYPE_OF_DELIVERY_LARGE_PACKAGE:
                ratePerMile = EngineUtils.RATE_PER_MILE_LARGE_PACKAGE;
                break;
            case EngineUtils.TYPE_OF_DELIVERY_FOOD_DELIVERY:
                ratePerMile = EngineUtils.RATE_PER_MILE_FOOD_DELIVERY;
                break;
            case EngineUtils.TYPE_OF_DELIVERY_EXPRESS:
                ratePerMile = EngineUtils.RATE_PER_MILE_EXPRESS;
                break;
            default:
                ratePerMile = EngineUtils.RATE_PER_MILE_NORMAL;
        }

        return (ratePerMile * distanceInMiles);
    }

    @Override
    public double calculateDeliveryPartnerFare(double totalFare, String typeOfDelivery) {
        double shareFactor;
        switch (typeOfDelivery.toUpperCase()) {
            case EngineUtils.TYPE_OF_DELIVERY_LARGE_PACKAGE:
                shareFactor = EngineUtils.SHARE_FOR_LARGE_PACKAGE;
                break;
            case EngineUtils.TYPE_OF_DELIVERY_FOOD_DELIVERY:
                shareFactor = EngineUtils.SHARE_FOR_FOOD_DELIVERY;
                break;
            case EngineUtils.TYPE_OF_DELIVERY_EXPRESS:
                shareFactor = EngineUtils.SHARE_FOR_EXPRESS;
                break;
            default:
                shareFactor = EngineUtils.SHARE_FOR_NORMAL;
        }

        return (shareFactor * totalFare);
    }
}