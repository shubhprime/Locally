package com.locally.backend.repository;

import com.locally.backend.model.Delivery;
import com.locally.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findBySenderId(Long senderId);
    Optional<Delivery> findByAssignedDeliveryPartnerId(Long assignedDeliveryPartnerId);
    List<Delivery> findAllBySenderOrderByCreatedAtDesc(User sender);
}