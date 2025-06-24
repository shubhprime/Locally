package com.locally.locally_backend_engine.repository;

import com.locally.locally_backend_engine.model.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {
    Optional<DeliveryAssignment> findByDeliveryId(Long deliveryId);
}