package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.dto.*;
import com.locally.locally_backend_engine.model.Delivery;
import com.locally.locally_backend_engine.repository.DeliveryRepository;
import com.locally.locally_backend_engine.service.DeliveryService;
import com.locally.locally_backend_engine.utils.EngineUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Transactional
    @Override
    public DeliveryEngineResponse createDelivery(DeliveryEngineRequest deliveryEngineRequest) {

        double deliveryFee = calculateFare(deliveryEngineRequest.getDistanceInMiles(), deliveryEngineRequest.getTypeOfDelivery());

        Delivery delivery = Delivery.builder()
                .senderId(deliveryEngineRequest.getSenderId())
                .pickUpAddress(deliveryEngineRequest.getPickUpAddress())
                .dropOffAddress(deliveryEngineRequest.getDropOffAddress())
                .packageDetails(deliveryEngineRequest.getPackageDetails())
                .typeOfDelivery(deliveryEngineRequest.getTypeOfDelivery())
                .deliveryStatus(EngineUtils.DELIVERY_STATUS_PENDING)
                .assignedDeliveryPartnerId(null)
                .deliveryFee(deliveryFee)
                .deliveryPartnerTravelFee(0)
                .totalFee(deliveryFee)
                .distanceInMiles(deliveryEngineRequest.getDistanceInMiles())
                .isPaid(false)
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        return DeliveryEngineResponse.builder()
                .responseCode(EngineUtils.CREATE_DELIVERY_SUCCESS_CODE)
                .success(EngineUtils.CREATE_DELIVERY_SUCCESS_SUCCESS)
                .responseMessage(EngineUtils.CREATE_DELIVERY_SUCCESS_MESSAGE)
                .deliveryId(savedDelivery.getDeliveryId())
                .build();
    }

    @Override
    public PaginatedDeliveryEngineResponse getAllDeliveriesForUser(Long senderId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Delivery> deliveryPage = deliveryRepository.findAllBySenderId(senderId, pageable);

        List<AllUserDeliveryEngineResponse> pagedDeliveries = deliveryPage.getContent().stream()
                .map(delivery -> AllUserDeliveryEngineResponse.builder()
                        .deliveryId(delivery.getDeliveryId())
                        .pickUpAddress(delivery.getPickUpAddress())
                        .dropOffAddress(delivery.getDropOffAddress())
                        .packageDetails(delivery.getPackageDetails())
                        .typeOfDelivery(delivery.getTypeOfDelivery())
                        .deliveryStatus(delivery.getDeliveryStatus())
                        .totalFee(delivery.getTotalFee())
                        .createdAt(delivery.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PaginatedDeliveryEngineResponse.builder()
                .deliveries(pagedDeliveries)
                .currentPage(page)
                .pageSize(size)
                .totalItems((int) deliveryPage.getTotalElements())
                .totalPages(deliveryPage.getTotalPages())
                .build();
    }

    @Override
    public UserDeliveryByIdEngineResponse getDeliveryByIdForUser(Long senderId, Long deliveryId) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if(!delivery.getSenderId().equals(senderId)) {
            throw new RuntimeException("Unauthorized access");
        }

        return UserDeliveryByIdEngineResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .pickUpAddress(delivery.getPickUpAddress())
                .dropOffAddress(delivery.getDropOffAddress())
                .packageDetails(delivery.getPackageDetails())
                .typeOfDelivery(delivery.getTypeOfDelivery())
                .deliveryStatus(delivery.getDeliveryStatus())
                .totalFee(delivery.getTotalFee())
                .createdAt(delivery.getCreatedAt())
                .build();
    }

    @Transactional
    @Override
    public DeliveryEngineResponse updateDelivery(UpdatedDeliveryEngineRequest updatedDeliveryEngineRequest) {

        Delivery existingDelivery = deliveryRepository.findById(updatedDeliveryEngineRequest.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        // Check user authorization (sender owns the delivery)
        if (!existingDelivery.getSenderId().equals(updatedDeliveryEngineRequest.getSenderId())) {
            throw new RuntimeException("Unauthorized to update this delivery");
        }

        // Check if delivery status is either pending or assigned
        String currentStatus = existingDelivery.getDeliveryStatus();
        if (!EngineUtils.DELIVERY_STATUS_PENDING.equals(currentStatus) &&
                !EngineUtils.DELIVERY_STATUS_ASSIGNED.equals(currentStatus) &&
                !EngineUtils.DELIVERY_STATUS_IN_TRANSIT.equals(currentStatus)) {
            throw new RuntimeException("Delivery cannot be updated once completed or cancelled. Only deliveries with status Pending, Assigned, or In Transit may be updated.");
        }

        double deliveryFee = calculateFare(updatedDeliveryEngineRequest.getUpdatedDistanceInMiles(), updatedDeliveryEngineRequest.getUpdatedTypeOfDelivery());

        existingDelivery.setPickUpAddress(updatedDeliveryEngineRequest.getUpdatedPickUpAddress());
        existingDelivery.setDropOffAddress(updatedDeliveryEngineRequest.getUpdatedDropOffAddress());
        existingDelivery.setPackageDetails(updatedDeliveryEngineRequest.getUpdatedPackageDetails());
        existingDelivery.setTypeOfDelivery(updatedDeliveryEngineRequest.getUpdatedTypeOfDelivery());
        existingDelivery.setDeliveryFee(deliveryFee);
        existingDelivery.setDeliveryPartnerTravelFee(0.0); // TODO: Add fare of distance travelled by the driver from the original starting point to new pickup point
        existingDelivery.setTotalFee(deliveryFee);
        existingDelivery.setDistanceInMiles(updatedDeliveryEngineRequest.getUpdatedDistanceInMiles());
        existingDelivery.setModifiedAt(LocalDateTime.now());

        Delivery updatedOrder = deliveryRepository.save(existingDelivery);

        return DeliveryEngineResponse.builder()
                .responseCode(EngineUtils.UPDATE_DELIVERY_SUCCESS_CODE)
                .success(EngineUtils.UPDATE_DELIVERY_SUCCESS_SUCCESS)
                .responseMessage(EngineUtils.UPDATE_DELIVERY_SUCCESS_MESSAGE)
                .deliveryId(updatedOrder.getDeliveryId())
                .build();
    }

    @Transactional
    @Override
    public DeliveryEngineResponse cancelDelivery(CancelDeliveryEngineRequest cancelDeliveryEngineRequest) {

        Delivery existingDelivery = deliveryRepository.findById(cancelDeliveryEngineRequest.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        // Check user authorization (sender owns the delivery)
        if (!existingDelivery.getSenderId().equals(cancelDeliveryEngineRequest.getSenderId())) {
            throw new RuntimeException("Unauthorized to cancel this delivery");
        }

        // Check if delivery status is either pending or assigned
        String currentStatus = existingDelivery.getDeliveryStatus();
        if (!EngineUtils.DELIVERY_STATUS_PENDING.equals(currentStatus) &&
                !EngineUtils.DELIVERY_STATUS_ASSIGNED.equals(currentStatus)) {
            throw new RuntimeException("Delivery can only be canceled if it is Pending or Assigned.");
        }

        existingDelivery.setDeliveryStatus(EngineUtils.DELIVERY_STATUS_CANCELLED);
        existingDelivery.setModifiedAt(LocalDateTime.now());
        existingDelivery.setCancelledAt(LocalDateTime.now());

        deliveryRepository.save(existingDelivery);

        return DeliveryEngineResponse.builder()
                .responseCode(EngineUtils.CANCEL_DELIVERY_SUCCESS_CODE)
                .success(EngineUtils.CANCEL_DELIVERY_SUCCESS_SUCCESS)
                .responseMessage(EngineUtils.CANCEL_DELIVERY_SUCCESS_MESSAGE)
                .deliveryId(existingDelivery.getDeliveryId())
                .build();
    }

//    @Override
//    public updateDeliveryStatus(String email, Long id, String newDeliveryStatus) {
//
//        Delivery existingDelivery = deliveryRepository.findById()
//                .orElseThrow(() -> new RuntimeException("Delivery not found"));
//
//        // Check user authorization (sender owns the delivery)
//        if (!existingDelivery.getSender().getId().equals(user.getId())) {
//            throw new RuntimeException("Unauthorized to update this delivery");
//        }
//
//        // Validate newStatus is valid and allowed for this user
//        switch(newDeliveryStatus) {
//            case "ASSIGNED":
//
//        }
//    }

    public double calculateFare(double distanceInMiles, String typeOfDelivery) {

        double ratePerMile;
        switch (typeOfDelivery.toUpperCase()) {
            case EngineUtils.TYPE_OF_DELIVERY_LARGE_PACKAGE:
                ratePerMile = EngineUtils.RATE_PER_MILE_LARGE_PACKAGE;
                break;
            case EngineUtils.TYPE_OF_DELIVERY_FOOD_DELIVERY:
                ratePerMile = EngineUtils.RATE_PER_MILE_FOOD_DELIVERY;
                break;
            case EngineUtils.TYPE_OF_DELIVERY_EXPRESS:
                ratePerMile = EngineUtils.RATE_PER_MILE_EXPRESS;
                break;
            default:
                ratePerMile = EngineUtils.RATE_PER_MILE_NORMAL;
        }

        return (ratePerMile * distanceInMiles);
    }
}