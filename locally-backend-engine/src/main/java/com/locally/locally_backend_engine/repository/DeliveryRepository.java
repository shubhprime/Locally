package com.locally.locally_backend_engine.repository;

import com.locally.locally_backend_engine.model.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findBySenderId(Long senderId);
    Page<Delivery> findAllBySenderId(Long senderId, Pageable pageable);
    Optional<Delivery> findByAssignedDeliveryPartnerId(Long assignedDeliveryPartnerId);
    List<Delivery> findAllBySenderIdOrderByCreatedAtDesc(Long senderId);
    List<Delivery> findAllByAssignedDeliveryPartnerIdOrderByCreatedAtDesc(Long assignedDeliveryPartnerId);
}