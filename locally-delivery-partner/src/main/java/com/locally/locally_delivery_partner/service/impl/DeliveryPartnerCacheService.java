package com.locally.locally_delivery_partner.service.impl;

import com.locally.locally_delivery_partner.model.DeliveryPartner;
import com.locally.locally_delivery_partner.repository.DeliveryPartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeliveryPartnerCacheService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Cacheable(value = "deliveryPartners", key = "#email")
    public Optional<DeliveryPartner> getCachedDeliveryPartnerByEmail(String email) {
        return deliveryPartnerRepository.findByEmail(email);
    }

    @Cacheable(value = "deliveryPartners", key = "#phoneNumber")
    public Optional<DeliveryPartner> getCachedDeliveryPartnerByPhone(String phoneNumber) {
        return deliveryPartnerRepository.findByPhoneNumber(phoneNumber);
    }
}