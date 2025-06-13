package com.locally.locally_delivery_partner.repository;

import com.locally.locally_delivery_partner.model.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    boolean existsByEmail(String email);
    Optional<DeliveryPartner> findByEmail(String email);
    Optional<DeliveryPartner> findByPhoneNumber(String phoneNumber);
    Optional<DeliveryPartner> findByEmailAndIsDeletedFalse(String email);
}