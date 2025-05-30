package com.locally.backend.service.impl;

import com.locally.backend.dto.DeliveryRequest;
import com.locally.backend.dto.DeliveryResponse;
import com.locally.backend.dto.GetUserDeliveryResponse;
import com.locally.backend.dto.UpdatedDeliveryRequest;
import com.locally.backend.model.Delivery;
import com.locally.backend.model.User;
import com.locally.backend.repository.DeliveryRepository;
import com.locally.backend.repository.UserRepository;
import com.locally.backend.service.DeliveryService;
import com.locally.backend.utils.AccountUtils;
import com.locally.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    @Override
    public DeliveryResponse createDelivery(DeliveryRequest deliveryRequest, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Doesn't Exist"));

        double deliveryFee = calculateFare(deliveryRequest.getDistanceInMiles(), deliveryRequest.getTypeOfDelivery());

        Delivery request =Delivery.builder()
                .sender(user)
                .pickUpAddress(deliveryRequest.getPickUpAddress())
                .dropOffAddress(deliveryRequest.getDropOffAddress())
                .packageDetails(deliveryRequest.getPackageDetails())
                .typeOfDelivery(deliveryRequest.getTypeOfDelivery())
                .deliveryStatus(AccountUtils.DELIVERY_STATUS_PENDING)
                .assignedDeliveryPartner(null) // no one assigned yet
                .deliveryFee(deliveryFee)
                .deliveryPartnerTravelFee(0.0) // will be updated later
                .totalFee(deliveryFee) // will be updated later
                .distanceInMiles(deliveryRequest.getDistanceInMiles())
                .isPaid(false)
                .build();

        Delivery order = deliveryRepository.save(request);

        return DeliveryResponse.builder()
                .responseCode(AccountUtils.CREATE_DELIVERY_SUCCESS_CODE)
                .success(AccountUtils.CREATE_DELIVERY_SUCCESS_SUCCESS)
                .responseMessage(AccountUtils.CREATE_DELIVERY_SUCCESS_MESSAGE)
                .id(order.getId())
                .build();
    }

    @Override
    public List<GetUserDeliveryResponse> getAllDeliveriesForUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Doesn't Exist"));

        return deliveryRepository.findAllBySenderOrderByCreatedAtDesc(user).stream()
                .map(delivery -> GetUserDeliveryResponse.builder()
                        .id(delivery.getId())
                        .pickUpAddress(delivery.getPickUpAddress())
                        .dropOffAddress(delivery.getDropOffAddress())
                        .packageDetails(delivery.getPackageDetails())
                        .typeOfDelivery(delivery.getTypeOfDelivery())
                        .deliveryStatus(delivery.getDeliveryStatus())
                        .totalFee(delivery.getTotalFee())
                        .createdAt(delivery.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public DeliveryResponse updateDelivery(Long id, UpdatedDeliveryRequest updatedDeliveryRequest, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Doesn't Exist"));

        Delivery existingDelivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        // Check user authorization (sender owns the delivery)
        if (!existingDelivery.getSender().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this delivery");
        }

        // Check if delivery status is either pending or assigned
        String currentStatus = existingDelivery.getDeliveryStatus();
        if (!AccountUtils.DELIVERY_STATUS_PENDING.equals(currentStatus) &&
                !AccountUtils.DELIVERY_STATUS_ASSIGNED.equals(currentStatus) &&
                !AccountUtils.DELIVERY_STATUS_IN_TRANSIT.equals(currentStatus)) {
            throw new RuntimeException("Delivery cannot be updated once completed or cancelled. Only deliveries with status Pending, Assigned, or In Transit may be updated.");
        }

        double deliveryFee = calculateFare(updatedDeliveryRequest.getUpdatedDistanceInMiles(), updatedDeliveryRequest.getUpdatedTypeOfDelivery());

        existingDelivery.setPickUpAddress(updatedDeliveryRequest.getUpdatedPickUpAddress());
        existingDelivery.setDropOffAddress(updatedDeliveryRequest.getUpdatedDropOffAddress());
        existingDelivery.setPackageDetails(updatedDeliveryRequest.getUpdatedPackageDetails());
        existingDelivery.setTypeOfDelivery(updatedDeliveryRequest.getUpdatedTypeOfDelivery());
        existingDelivery.setDeliveryFee(deliveryFee);
        existingDelivery.setDeliveryPartnerTravelFee(0.0); // TODO: Add fare of distance travelled by the driver from the original starting point to new pickup point
        existingDelivery.setTotalFee(deliveryFee);
        existingDelivery.setDistanceInMiles(updatedDeliveryRequest.getUpdatedDistanceInMiles());
        existingDelivery.setModifiedAt(LocalDateTime.now());

        Delivery updatedOrder = deliveryRepository.save(existingDelivery);

        return DeliveryResponse.builder()
                .responseCode(AccountUtils.UPDATE_DELIVERY_SUCCESS_CODE)
                .success(AccountUtils.UPDATE_DELIVERY_SUCCESS_SUCCESS)
                .responseMessage(AccountUtils.UPDATE_DELIVERY_SUCCESS_MESSAGE)
                .id(updatedOrder.getId())
                .build();
    }

    @Transactional
    @Override
    public DeliveryResponse cancelDelivery(Long id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Doesn't Exist"));

        Delivery existingDelivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        // Check user authorization (sender owns the delivery)
        if (!existingDelivery.getSender().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this delivery");
        }

        // Check if delivery status is either pending or assigned
        String currentStatus = existingDelivery.getDeliveryStatus();
        if (!AccountUtils.DELIVERY_STATUS_PENDING.equals(currentStatus) &&
                !AccountUtils.DELIVERY_STATUS_ASSIGNED.equals(currentStatus)) {
            throw new RuntimeException("Delivery can only be canceled if it is Pending or Assigned.");
        }

        existingDelivery.setDeliveryStatus(AccountUtils.DELIVERY_STATUS_CANCELLED);
        existingDelivery.setModifiedAt(LocalDateTime.now());
        existingDelivery.setCancelledAt(LocalDateTime.now());

        deliveryRepository.save(existingDelivery);

        return DeliveryResponse.builder()
                .responseCode(AccountUtils.CANCEL_DELIVERY_SUCCESS_CODE)
                .success(AccountUtils.CANCEL_DELIVERY_SUCCESS_SUCCESS)
                .responseMessage(AccountUtils.CANCEL_DELIVERY_SUCCESS_MESSAGE)
                .id(existingDelivery.getId())
                .build();
    }

    public double calculateFare(double distanceInMiles, String typeOfDelivery) {

        double ratePerMile;
        switch (typeOfDelivery.toUpperCase()) {
            case AccountUtils.TYPE_OF_DELIVERY_LARGE_PACKAGE:
                ratePerMile = AccountUtils.RATE_PER_MILE_LARGE_PACKAGE;
                break;
            case AccountUtils.TYPE_OF_DELIVERY_FOOD_DELIVERY:
                ratePerMile = AccountUtils.RATE_PER_MILE_FOOD_DELIVERY;
                break;
            case AccountUtils.TYPE_OF_DELIVERY_EXPRESS:
                ratePerMile = AccountUtils.RATE_PER_MILE_EXPRESS;
                break;
            default:
                ratePerMile = AccountUtils.RATE_PER_MILE_NORMAL;
        }

        return (ratePerMile * distanceInMiles);
    }
}