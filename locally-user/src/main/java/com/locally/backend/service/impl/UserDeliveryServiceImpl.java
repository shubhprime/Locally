package com.locally.backend.service.impl;

import com.locally.backend.client.DeliveryClient;
import com.locally.backend.dto.*;
import com.locally.backend.model.User;
import com.locally.backend.repository.UserRepository;
import com.locally.backend.service.UserDeliveryService;
import com.locally.backend.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDeliveryServiceImpl implements UserDeliveryService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeliveryClient deliveryClient;

    @Override
    public DeliveryResponse createDelivery(DeliveryRequest deliveryRequest, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        deliveryRequest.setSenderId(user.getId());

        return deliveryClient.createDelivery(deliveryRequest);
    }

    @Override
    public PaginatedDeliveryResponse getAllDeliveriesForUser(String email, int page, int size) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        return deliveryClient.getAllDeliveriesForUser(user.getId(), page, size);
    }

    @Override
    public UserDeliveryByIdResponse getDeliveryByIdForUser(String email, Long deliveryId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        return deliveryClient.getDeliveryByIdForUser(deliveryId, user.getId());
    }

    @Override
    public DeliveryResponse updateDelivery(Long deliveryId, UpdatedDeliveryRequest updatedDeliveryRequest, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        updatedDeliveryRequest.setSenderId(user.getId());
        updatedDeliveryRequest.setDeliveryId(deliveryId);

        return deliveryClient.updateDelivery(updatedDeliveryRequest);
    }

    @Override
    public DeliveryResponse cancelDelivery(Long deliveryId, CancelDeliveryRequest cancelDeliveryRequest, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        cancelDeliveryRequest.setSenderId(user.getId());
        cancelDeliveryRequest.setDeliveryId(deliveryId);

        return deliveryClient.cancelDelivery(cancelDeliveryRequest);
    }
}