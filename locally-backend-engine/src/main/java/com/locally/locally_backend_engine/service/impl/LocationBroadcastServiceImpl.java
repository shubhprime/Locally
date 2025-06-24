package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.dto.DeliveryPartnerLocationPayload;
import com.locally.locally_backend_engine.dto.DeliveryPartnerLocationRequest;
import com.locally.locally_backend_engine.dto.DeliveryPartnerLocationResponse;
import com.locally.locally_backend_engine.model.Delivery;
import com.locally.locally_backend_engine.model.DeliveryStatus;
import com.locally.locally_backend_engine.repository.DeliveryRepository;
import com.locally.locally_backend_engine.service.LocationBroadcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LocationBroadcastServiceImpl implements LocationBroadcastService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private RedisTemplate<String, Object>  redisTemplate;

    @Override
    public void broadcastLocation(DeliveryPartnerLocationRequest deliveryPartnerLocationRequest) {

        Delivery delivery = deliveryRepository.findById(deliveryPartnerLocationRequest.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (!List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.IN_TRANSIT).contains(delivery.getDeliveryStatus())) {
            throw new RuntimeException("Delivery is not active.");
        }

        if (!deliveryPartnerLocationRequest.getDeliveryPartnerId().equals(delivery.getAssignedDeliveryPartnerId())) {
            throw new RuntimeException("Unauthorized: This delivery is not assigned to the given partner.");
        }

        String phase = delivery.getDeliveryStatus() == DeliveryStatus.ASSIGNED ? "TO_PICKUP" : "TO_DROPOFF";

        DeliveryPartnerLocationPayload payload = DeliveryPartnerLocationPayload.builder()
                .deliveryId(deliveryPartnerLocationRequest.getDeliveryId())
                .deliveryPartnerId(deliveryPartnerLocationRequest.getDeliveryPartnerId())
                .latitude(deliveryPartnerLocationRequest.getLatitude())
                .longitude(deliveryPartnerLocationRequest.getLongitude())
                .phase(phase)
                .build();

        // Push to WebSocket topic
        // Broadcast if everything checks out
        simpMessagingTemplate.convertAndSend("/topic/location/" + deliveryPartnerLocationRequest.getDeliveryId(), payload);

        // Save to Redis
        String redisKey = "location:delivery:" + deliveryPartnerLocationRequest.getDeliveryId();
        redisTemplate.opsForValue().set(redisKey, payload, 2, TimeUnit.MINUTES);
    }

    @Override
    public DeliveryPartnerLocationResponse getLatestLocation(Long deliveryId) {

        String redisKey = "location:delivery:" + deliveryId;

        DeliveryPartnerLocationPayload cached = (DeliveryPartnerLocationPayload)
                redisTemplate.opsForValue().get(redisKey);

        if (cached == null) {
            throw new RuntimeException("No recent location available for this delivery.");
        }

        return DeliveryPartnerLocationResponse.builder()
                .deliveryId(deliveryId)
                .deliveryPartnerId(cached.getDeliveryPartnerId())
                .latitude(cached.getLatitude())
                .longitude(cached.getLongitude())
                .phase(cached.getPhase())
                .build();
    }

}