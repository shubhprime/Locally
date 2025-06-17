package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.dto.*;
import com.locally.locally_backend_engine.model.Delivery;
import com.locally.locally_backend_engine.model.DeliveryStatus;
import com.locally.locally_backend_engine.repository.DeliveryRepository;
import com.locally.locally_backend_engine.service.DeliveryPartnerDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryPartnerDeliveryServiceImpl implements DeliveryPartnerDeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Transactional
    @Override
    public DeliveryEngineResponse acceptDelivery(DeliveryStatusRequest deliveryStatusRequest) {

        Delivery delivery = deliveryRepository.findById(deliveryStatusRequest.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (!DeliveryStatus.PENDING.equals(delivery.getDeliveryStatus())) {
            throw new RuntimeException("Delivery cannot be accepted. Current status: " + delivery.getDeliveryStatus());
        }

        delivery.setAssignedDeliveryPartnerId(deliveryStatusRequest.getDeliveryPartnerId());
        delivery.setDeliveryStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());

        deliveryRepository.save(delivery);

        return DeliveryEngineResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Delivery accepted successfully")
                .deliveryId(deliveryStatusRequest.getDeliveryId())
                .build();
    }

    @Transactional
    @Override
    public DeliveryEngineResponse markDeliveryInTransit(DeliveryStatusRequest deliveryStatusRequest) {

        Delivery delivery = deliveryRepository.findById(deliveryStatusRequest.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (!deliveryStatusRequest.getDeliveryPartnerId().equals(delivery.getAssignedDeliveryPartnerId())) {
            throw new RuntimeException("Unauthorized: This delivery is not assigned to you");
        }

        if (!DeliveryStatus.ASSIGNED.equals(delivery.getDeliveryStatus())) {
            throw new RuntimeException("Cannot mark as in-transit. Current status: " + delivery.getDeliveryStatus());
        }

        delivery.setDeliveryStatus(DeliveryStatus.IN_TRANSIT);
        delivery.setInTransitAt(LocalDateTime.now());

        deliveryRepository.save(delivery);

        return DeliveryEngineResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Delivery marked as in-transit")
                .deliveryId(deliveryStatusRequest.getDeliveryId())
                .build();
    }

    @Transactional
    @Override
    public DeliveryEngineResponse markDeliveryDelivered(DeliveryStatusRequest deliveryStatusRequest) {

        Delivery delivery = deliveryRepository.findById(deliveryStatusRequest.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (!deliveryStatusRequest.getDeliveryPartnerId().equals(delivery.getAssignedDeliveryPartnerId())) {
            throw new RuntimeException("Unauthorized: This delivery is not assigned to you");
        }

        if (!DeliveryStatus.IN_TRANSIT.equals(delivery.getDeliveryStatus())) {
            throw new RuntimeException("Cannot mark as delivered. Current status: " + delivery.getDeliveryStatus());
        }

        delivery.setDeliveryStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        return DeliveryEngineResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Delivery marked as delivered")
                .deliveryId(deliveryStatusRequest.getDeliveryId())
                .build();
    }

    @Transactional
    @Override
    public DeliveryEngineResponse markDeliveryPaid(DeliveryStatusRequest deliveryStatusRequest) {

        Delivery delivery = deliveryRepository.findById(deliveryStatusRequest.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (!deliveryStatusRequest.getDeliveryPartnerId().equals(delivery.getAssignedDeliveryPartnerId())) {
            throw new RuntimeException("Unauthorized: This delivery is not assigned to you");
        }

        if (!DeliveryStatus.DELIVERED.equals(delivery.getDeliveryStatus())) {
            throw new RuntimeException("Delivery must be marked as delivered first. Current status: " + delivery.getDeliveryStatus());
        }

        if (delivery.isPaid()) {
            throw new RuntimeException("Delivery has already been marked as paid.");
        }

        delivery.setPaid(true);
        delivery.setDeliveryStatus(DeliveryStatus.PAID);
        delivery.setPaidAt(LocalDateTime.now());
        delivery.setModifiedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        return DeliveryEngineResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Delivery marked as paid")
                .deliveryId(deliveryStatusRequest.getDeliveryId())
                .build();
    }

    @Transactional
    @Override
    public DeliveryEngineResponse cancelDelivery(DeliveryStatusRequest deliveryStatusRequest) {

        Delivery delivery = deliveryRepository.findById(deliveryStatusRequest.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (!deliveryStatusRequest.getDeliveryPartnerId().equals(delivery.getAssignedDeliveryPartnerId())) {
            throw new RuntimeException("Unauthorized: This delivery is not assigned to you");
        }

        if (!DeliveryStatus.ASSIGNED.equals(delivery.getDeliveryStatus())) {
            throw new RuntimeException("Only ASSIGNED deliveries can be canceled by the partner. Current status: " + delivery.getDeliveryStatus());
        }

        if (deliveryStatusRequest.getCancellationReason() == null || deliveryStatusRequest.getCancellationReason().isBlank()) {
            throw new RuntimeException("Cancellation reason is required.");
        }

        delivery.setDeliveryStatus(DeliveryStatus.PENDING);
        delivery.setAssignedDeliveryPartnerId(null);
        delivery.setCancellationReason(deliveryStatusRequest.getCancellationReason());
        delivery.setCancelledAt(LocalDateTime.now());
        delivery.setModifiedAt(LocalDateTime.now());

        deliveryRepository.save(delivery);

        return DeliveryEngineResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Delivery cancelled and made available for reassignment")
                .deliveryId(delivery.getDeliveryId())
                .build();
    }

    @Transactional
    @Override
    public DeliveryEngineResponse markDeliveryFailed(DeliveryStatusRequest deliveryStatusRequest) {

        Delivery delivery = deliveryRepository.findById(deliveryStatusRequest.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (!deliveryStatusRequest.getDeliveryPartnerId().equals(delivery.getAssignedDeliveryPartnerId())) {
            throw new RuntimeException("Unauthorized: This delivery is not assigned to you");
        }

        if (!DeliveryStatus.IN_TRANSIT.equals(delivery.getDeliveryStatus())) {
            throw new RuntimeException("Only deliveries in-transit can be marked as failed. Current status: " + delivery.getDeliveryStatus());
        }

        if (deliveryStatusRequest.getFailureReason() == null || deliveryStatusRequest.getFailureReason().isBlank()) {
            throw new RuntimeException("Cancellation reason is required.");
        }

        delivery.setDeliveryStatus(DeliveryStatus.FAILED);
        delivery.setCancellationReason(deliveryStatusRequest.getFailureReason());
        delivery.setFailedAt(LocalDateTime.now());
        delivery.setModifiedAt(LocalDateTime.now());

        deliveryRepository.save(delivery);

        return DeliveryEngineResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Delivery cancelled by delivery partner")
                .deliveryId(delivery.getDeliveryId())
                .build();
    }

    @Override
     public DeliveryEngineResponse getActiveDeliveryForPartner(Long deliveryPartnerId) {

        List<DeliveryStatus> activeStatuses = List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.IN_TRANSIT, DeliveryStatus.DELIVERED);

        Delivery delivery = deliveryRepository
                .findByAssignedDeliveryPartnerIdAndDeliveryStatusIn(deliveryPartnerId, activeStatuses)
                .orElseThrow(() -> new RuntimeException("No active delivery found for this partner."));

        return DeliveryEngineResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Active delivery found")
                .deliveryId(delivery.getDeliveryId())
                .build();

    }
}