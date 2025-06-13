package com.locally.locally_delivery_partner.service.impl;

import com.locally.locally_delivery_partner.service.DeliveryPartnerStatusService;
import com.locally.locally_delivery_partner.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeliveryPartnerStatusServiceImpl  implements DeliveryPartnerStatusService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void markAvailable(String email) {
        redisUtil.setDeliveryPartnerStatus(email, "AVAILABLE", 240); // 4 hours
    }

    @Override
    public void markOnDelivery(String email) {
        redisUtil.setDeliveryPartnerStatus(email, "ON_DELIVERY");
    }

    @Override
    public void markUnavailable(String email) {
        redisUtil.setDeliveryPartnerStatus(email, "UNAVAILABLE"); // 12 hours
    }

    @Override
    public String getCurrentStatus(String email) {
        String status = redisUtil.getDeliveryPartnerStatus(email);
        return status != null ? status : "UNAVAILABLE"; // default fallback
    }
}