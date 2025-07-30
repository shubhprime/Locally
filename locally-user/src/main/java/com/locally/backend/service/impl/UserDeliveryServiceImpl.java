package com.locally.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locally.backend.client.UserDelivery.UserDeliveryClient;
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
    private UserDeliveryClient userDeliveryClient;

    @Override
    public DeliveryResponse createDelivery(DeliveryRequest deliveryRequest) {

        User user = userRepository.findByEmail(deliveryRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        if (user.getAccountHolderName() == null || user.getAccountHolderName().isBlank() ||
                user.getBankAccountNumber() == null || user.getBankAccountNumber().isBlank() ||
                user.getBankName() == null || user.getBankName().isBlank() ||
                user.getRoutingNumber() == null || user.getRoutingNumber().isBlank() ||
                user.getAccountType() == null || user.getAccountType().isBlank()) {

            return DeliveryResponse.builder()
                    .success(false)
                    .responseCode("400")
                    .responseMessage("Bank details must be completed before creating a delivery.")
                    .build();
        }

        deliveryRequest.setSenderId(user.getId());

        try {
            return userDeliveryClient.createDelivery(deliveryRequest);
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8() != null ? fe.contentUTF8() : "";
                if (errorBody.trim().startsWith("{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, DeliveryResponse.class);
                }
            } catch (Exception e) {
                System.err.println("Error parsing Feign error response: " + e.getMessage());
            }

            return DeliveryResponse.builder()
                    .success(false)
                    .responseCode(String.valueOf(fe.status()))
                    .responseMessage("Internal error or malformed error response from Engine")
                    .build();
        }
    }

    @Override
    public PaginatedDeliveryResponse getAllDeliveriesForUser(String email, int page, int size) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        try {
            return userDeliveryClient.getAllDeliveriesForUser(user.getId(), page, size);
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8();
                if (errorBody != null && !errorBody.isBlank()) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, PaginatedDeliveryResponse.class);
                }
            } catch (Exception parseException) {
                // Parsing failed; return a fallback error response
                return PaginatedDeliveryResponse.builder()
                        .currentPage(page)
                        .pageSize(size)
                        .totalItems(0)
                        .totalPages(0)
                        .build();
            }

            // If response body is blank or null
            return PaginatedDeliveryResponse.builder()
                    .currentPage(page)
                    .pageSize(size)
                    .totalItems(0)
                    .totalPages(0)
                    .build();
        }
    }

    @Override
    public UserDeliveryByIdResponse getDeliveryByIdForUser(String email, Long deliveryId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        try {
            return userDeliveryClient.getDeliveryByIdForUser(deliveryId, user.getId());
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8();
                if (errorBody != null && !errorBody.isBlank()) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, UserDeliveryByIdResponse.class);
                }
            } catch (Exception parseException) {
                return UserDeliveryByIdResponse.builder()
                        .build();
            }

            return UserDeliveryByIdResponse.builder()
                    .build();
        }
    }

    @Override
    public DeliveryResponse updateDelivery(Long deliveryId, UpdatedDeliveryRequest updatedDeliveryRequest, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        updatedDeliveryRequest.setSenderId(user.getId());
        updatedDeliveryRequest.setDeliveryId(deliveryId);

        try {
            return userDeliveryClient.updateDelivery(updatedDeliveryRequest);
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8() != null ? fe.contentUTF8() : "";
                if (errorBody.trim().startsWith("{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, DeliveryResponse.class);
                }
            } catch (Exception e) {
                System.err.println("Error parsing Feign error response: " + e.getMessage());
            }

            return DeliveryResponse.builder()
                    .success(false)
                    .responseCode(String.valueOf(fe.status()))
                    .responseMessage("Internal error or malformed error response from Engine")
                    .build();
        }
    }

    @Override
    public DeliveryResponse cancelDelivery(Long deliveryId, CancelDeliveryRequest cancelDeliveryRequest, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        cancelDeliveryRequest.setSenderId(user.getId());
        cancelDeliveryRequest.setDeliveryId(deliveryId);

        try {
            return userDeliveryClient.cancelDelivery(cancelDeliveryRequest);
        } catch (feign.FeignException fe) {
            try {
                String errorBody = fe.contentUTF8() != null ? fe.contentUTF8() : "";
                if (errorBody.trim().startsWith("{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(errorBody, DeliveryResponse.class);
                }
            } catch (Exception e) {
                System.err.println("Error parsing Feign error response: " + e.getMessage());
            }

            return DeliveryResponse.builder()
                    .success(false)
                    .responseCode(String.valueOf(fe.status()))
                    .responseMessage("Internal error or malformed error response from Engine")
                    .build();
        }
    }
}