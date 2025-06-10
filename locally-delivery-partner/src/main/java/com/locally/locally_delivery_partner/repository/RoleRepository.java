package com.locally.locally_delivery_partner.repository;

import com.locally.locally_delivery_partner.model.DeliveryPartnerRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<DeliveryPartnerRole, Long> {
    Optional<DeliveryPartnerRole> findByRoleName(String roleName);
}