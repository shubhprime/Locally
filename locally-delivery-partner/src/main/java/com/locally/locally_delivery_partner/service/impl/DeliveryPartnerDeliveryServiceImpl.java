package com.locally.locally_delivery_partner.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locally.locally_delivery_partner.client.DeliveryPartnerDelivery.DeliveryPartnerDeliveryClient;
import com.locally.locally_delivery_partner.dto.DeliveryEngineResponse;
import com.locally.locally_delivery_partner.dto.DeliveryStatusRequest;
import com.locally.locally_delivery_partner.model.DeliveryPartner;
import com.locally.locally_delivery_partner.repository.DeliveryPartnerRepository;
import com.locally.locally_delivery_partner.service.DeliveryPartnerDeliveryService;
import com.locally.locally_delivery_partner.utils.DeliveryPartnerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeliveryPartnerDeliveryServiceImpl implements DeliveryPartnerDeliveryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private DeliveryPartnerDeliveryClient deliveryPartnerDeliveryClient;

    @Override
    public DeliveryEngineResponse acceptDelivery(DeliveryStatusRequest deliveryStatusRequest) {

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(deliveryStatusRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        deliveryStatusRequest.setDeliveryPartnerId(deliveryPartner.getId());

        try {
            DeliveryEngineResponse deliveryEngineResponse = deliveryPartnerDeliveryClient.acceptDelivery(deliveryStatusRequest);

            if (Boolean.TRUE.equals(deliveryEngineResponse.isSuccess())) {
                redisTemplate.opsForValue().set(
                        "delivery_status:" + deliveryPartner.getId(),
                        "ON_DELIVERY",
                        2, java.util.concurrent.TimeUnit.HOURS
                );
            }

            return deliveryEngineResponse;
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8() != null ? fe.contentUTF8() : "";
                if (errorBody.trim().startsWith("{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, DeliveryEngineResponse.class);
                }
            } catch (Exception e) {
                System.err.println("Error parsing Feign error response: " + e.getMessage());
            }

            return DeliveryEngineResponse.builder()
                    .success(false)
                    .responseCode(String.valueOf(fe.status()))
                    .responseMessage("Internal error or malformed error response from Engine")
                    .build();
        }
    }

    @Override
    public DeliveryEngineResponse markDeliveryInTransit(DeliveryStatusRequest deliveryStatusRequest) {

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(deliveryStatusRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        deliveryStatusRequest.setDeliveryPartnerId(deliveryPartner.getId());

        try {
            return deliveryPartnerDeliveryClient.markDeliveryInTransit(deliveryStatusRequest);
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8() != null ? fe.contentUTF8() : "";
                if (errorBody.trim().startsWith("{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, DeliveryEngineResponse.class);
                }
            } catch (Exception e) {
                System.err.println("Error parsing Feign error response: " + e.getMessage());
            }

            return DeliveryEngineResponse.builder()
                    .success(false)
                    .responseCode(String.valueOf(fe.status()))
                    .responseMessage("Internal error or malformed error response from Engine")
                    .build();
        }
    }

    @Override
    public DeliveryEngineResponse markDeliveryDelivered(DeliveryStatusRequest deliveryStatusRequest) {

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(deliveryStatusRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        deliveryStatusRequest.setDeliveryPartnerId(deliveryPartner.getId());

        try {
            redisTemplate.delete("delivery_status:" + deliveryPartner.getId());

            return deliveryPartnerDeliveryClient.markDeliveryDelivered(deliveryStatusRequest);
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8() != null ? fe.contentUTF8() : "";
                if (errorBody.trim().startsWith("{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, DeliveryEngineResponse.class);
                }
            } catch (Exception e) {
                System.err.println("Error parsing Feign error response: " + e.getMessage());
            }

            return DeliveryEngineResponse.builder()
                    .success(false)
                    .responseCode(String.valueOf(fe.status()))
                    .responseMessage("Internal error or malformed error response from Engine")
                    .build();
        }
    }

    @Override
    public DeliveryEngineResponse markDeliveryPaid(DeliveryStatusRequest deliveryStatusRequest) {

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(deliveryStatusRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        deliveryStatusRequest.setDeliveryPartnerId(deliveryPartner.getId());

        try {
            return deliveryPartnerDeliveryClient.markDeliveryPaid(deliveryStatusRequest);
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8() != null ? fe.contentUTF8() : "";
                if (errorBody.trim().startsWith("{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, DeliveryEngineResponse.class);
                }
            } catch (Exception e) {
                System.err.println("Error parsing Feign error response: " + e.getMessage());
            }

            return DeliveryEngineResponse.builder()
                    .success(false)
                    .responseCode(String.valueOf(fe.status()))
                    .responseMessage("Internal error or malformed error response from Engine")
                    .build();
        }
    }

    @Override
    public DeliveryEngineResponse cancelDelivery(DeliveryStatusRequest deliveryStatusRequest) {

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(deliveryStatusRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        deliveryStatusRequest.setDeliveryPartnerId(deliveryPartner.getId());

        try {
            redisTemplate.delete("delivery_status:" + deliveryPartner.getId());

            return deliveryPartnerDeliveryClient.cancelDelivery(deliveryStatusRequest);
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8() != null ? fe.contentUTF8() : "";
                if (errorBody.trim().startsWith("{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, DeliveryEngineResponse.class);
                }
            } catch (Exception e) {
                System.err.println("Error parsing Feign error response: " + e.getMessage());
            }

            return DeliveryEngineResponse.builder()
                    .success(false)
                    .responseCode(String.valueOf(fe.status()))
                    .responseMessage("Internal error or malformed error response from Engine")
                    .build();
        }
    }

    @Override
    public DeliveryEngineResponse deliveryFailed(DeliveryStatusRequest deliveryStatusRequest) {

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(deliveryStatusRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        deliveryStatusRequest.setDeliveryPartnerId(deliveryPartner.getId());

        try {
            redisTemplate.delete("delivery_status:" + deliveryPartner.getId());

            return deliveryPartnerDeliveryClient.deliveryFailed(deliveryStatusRequest);
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8() != null ? fe.contentUTF8() : "";
                if (errorBody.trim().startsWith("{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, DeliveryEngineResponse.class);
                }
            } catch (Exception e) {
                System.err.println("Error parsing Feign error response: " + e.getMessage());
            }

            return DeliveryEngineResponse.builder()
                    .success(false)
                    .responseCode(String.valueOf(fe.status()))
                    .responseMessage("Internal error or malformed error response from Engine")
                    .build();
        }
    }

    @Override
    public DeliveryEngineResponse getActiveDelivery(Long deliveryPartnerId, String email) {

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        if (!deliveryPartner.getId().equals(deliveryPartnerId)) {
            throw new RuntimeException("Access denied: Partner ID does not match token");
        }

        try {
            return deliveryPartnerDeliveryClient.getActiveDelivery(deliveryPartnerId);
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8() != null ? fe.contentUTF8() : "";
                if (errorBody.trim().startsWith("{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, DeliveryEngineResponse.class);
                }
            } catch (Exception e) {
                System.err.println("Error parsing Feign error response: " + e.getMessage());
            }

            return DeliveryEngineResponse.builder()
                    .success(false)
                    .responseCode(String.valueOf(fe.status()))
                    .responseMessage("Internal error or malformed error response from Engine")
                    .build();
        }
    }
}