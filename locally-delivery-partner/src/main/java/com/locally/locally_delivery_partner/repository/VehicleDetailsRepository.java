package com.locally.locally_delivery_partner.repository;

import com.locally.locally_delivery_partner.model.VehicleDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleDetailsRepository extends JpaRepository<VehicleDetails, Long> {
    Optional<VehicleDetails> findByLicenseNumber(String licenseNumber);
    Optional<VehicleDetails> findByRegistrationNumber(String registrationNumber);
    List<VehicleDetails> findAllByDeliveryPartnerId(Long deliveryPartnerId);
}