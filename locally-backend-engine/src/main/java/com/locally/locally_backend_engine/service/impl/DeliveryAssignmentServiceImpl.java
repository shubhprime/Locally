package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.model.DeliveryAssignment;
import com.locally.locally_backend_engine.repository.DeliveryAssignmentRepository;
import com.locally.locally_backend_engine.service.DeliveryAssignmentService;
import com.locally.locally_backend_engine.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeliveryAssignmentServiceImpl implements DeliveryAssignmentService {

    @Autowired
    private DeliveryAssignmentRepository deliveryAssignmentRepository;

    @Autowired
    private RedisUtil redisUtil;

    private static final String ASSIGNMENT_KEY_PREFIX = "delivery_assigned:";

    private static final Logger logger = LoggerFactory.getLogger(DeliveryAssignmentServiceImpl.class);

    @Override
    public void assign(Long deliveryPartnerId, Long deliveryId) {

        // Prevent duplicate assignment
        deliveryAssignmentRepository.findByDeliveryId(deliveryId).ifPresent(existing -> {
            throw new IllegalStateException("Delivery already assigned to: " + existing.getDeliveryPartnerId());
        });

        // Save to database
        DeliveryAssignment entity = DeliveryAssignment.builder()
                .deliveryId(deliveryId)
                .deliveryPartnerId(deliveryPartnerId)
                .assignedAt(LocalDateTime.now())
                .completed(false)
                .build();
        deliveryAssignmentRepository.save(entity);

        redisUtil.setWithExpiry(
                ASSIGNMENT_KEY_PREFIX + deliveryId,
                deliveryPartnerId.toString(),
                300); // 5 minutes TTl

        // Optionally update delivery partner status
        redisUtil.setDeliveryPartnerStatus(deliveryPartnerId.toString(), "ON_DELIVERY");

        logger.info("Assigned deliveryId {} to deliveryPartnerId {}", deliveryId, deliveryPartnerId);
    }

    // TODO: NEED TO IMPLEMENT CALL
    @Override
    public void markCompleted(Long deliveryId) {
        deliveryAssignmentRepository.findByDeliveryId(deliveryId).ifPresent(assign -> {
            assign.setCompleted(true);
            deliveryAssignmentRepository.save(assign);
            logger.info("Marked deliveryId {} as completed", deliveryId);
        });
        redisUtil.delete(ASSIGNMENT_KEY_PREFIX + deliveryId);
    }
}