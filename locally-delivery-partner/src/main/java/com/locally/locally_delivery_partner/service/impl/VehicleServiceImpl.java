package com.locally.locally_delivery_partner.service.impl;

import com.locally.locally_delivery_partner.dto.*;
import com.locally.locally_delivery_partner.model.DeliveryPartner;
import com.locally.locally_delivery_partner.model.VehicleDetails;
import com.locally.locally_delivery_partner.repository.DeliveryPartnerRepository;
import com.locally.locally_delivery_partner.repository.VehicleDetailsRepository;
import com.locally.locally_delivery_partner.service.VehicleService;
import com.locally.locally_delivery_partner.utils.DeliveryPartnerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private VehicleDetailsRepository vehicleDetailsRepository;

    @Override
    public DeliveryPartnerResponse createVehicle(CreateVehicleRequest createVehicleRequest) {
        /**
         * Create a new vehicle and save it into the database
         */

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(createVehicleRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        // Check how many vehicles already exist for this partner
        List<VehicleDetails> existingVehicles = vehicleDetailsRepository.findAllByDeliveryPartnerId(deliveryPartner.getId());

        if (vehicleDetailsRepository.findByLicenseNumber(createVehicleRequest.getLicenseNumber()).isPresent()) {
            throw new RuntimeException("Vehicle with this license number already exists.");
        }

        if (vehicleDetailsRepository.findByRegistrationNumber(createVehicleRequest.getRegistrationNumber()).isPresent()) {
            throw new RuntimeException("Vehicle with this registration number already exists.");
        }


        boolean isPrimary = existingVehicles.isEmpty();

        VehicleDetails vehicle = VehicleDetails.builder()
                .deliveryPartner(deliveryPartner)
                .vehicleType(createVehicleRequest.getVehicleType())
                .vehicleColor(createVehicleRequest.getVehicleColor())
                .vehicleModel(createVehicleRequest.getVehicleModel())
                .registrationNumber(createVehicleRequest.getRegistrationNumber())
                .licenseNumber(createVehicleRequest.getLicenseNumber())
                .isPrimaryVehicle(isPrimary)
                .build();

        vehicleDetailsRepository.save(vehicle);

        return DeliveryPartnerResponse.builder()
                .responseCode(DeliveryPartnerUtils.VEHICLE_CREATION_CODE)
                .success(DeliveryPartnerUtils.VEHICLE_CREATION_SUCCESS)
                .responseMessage(isPrimary ? DeliveryPartnerUtils.PRIMARY_VEHICLE_CREATION_MESSAGE : DeliveryPartnerUtils.SECONDARY_VEHICLE_CREATION_MESSAGE)
                .build();
    }

    @Override
    public List<VehicleResponse> listVehicles(VehicleListRequest vehicleListRequest) {
        /**
         * Get the list of all vehicles for a user
         */

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(vehicleListRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        List<VehicleDetails> vehicleDetails = vehicleDetailsRepository.findAllByDeliveryPartnerId(deliveryPartner.getId());

        // Sort vehicles: primary vehicle first
        vehicleDetails.sort(Comparator.comparing(VehicleDetails::getIsPrimaryVehicle).reversed());

        return vehicleDetails.stream().map(vehicle ->
                VehicleResponse.builder()
                        .vehicleId(vehicle.getId())
                        .vehicleType(vehicle.getVehicleType())
                        .vehicleColor(vehicle.getVehicleColor())
                        .vehicleModel(vehicle.getVehicleModel())
                        .registrationNumber(vehicle.getRegistrationNumber())
                        .licenseNumber(vehicle.getLicenseNumber())
                        .isPrimaryVehicle(vehicle.getIsPrimaryVehicle())
                        .build()
        ).toList();
    }

    @Override
    public DeliveryPartnerResponse updateVehicle(UpdateVehicleRequest updateVehicleRequest) {
        /**
         * Update a vehicle's details
         */

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(updateVehicleRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        VehicleDetails vehicleDetails = vehicleDetailsRepository.findById(updateVehicleRequest.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicleDetails.getDeliveryPartner().getId().equals(deliveryPartner.getId())) {
            throw new RuntimeException("You are not authorized to update this vehicle.");
        }

        if (updateVehicleRequest.getVehicleColor() != null) {
            vehicleDetails.setVehicleColor(updateVehicleRequest.getVehicleColor());
        }
        if (updateVehicleRequest.getVehicleModel() != null) {
            vehicleDetails.setVehicleModel(updateVehicleRequest.getVehicleModel());
        }

        // Optional: Make this vehicle the primary vehicle
        if (Boolean.TRUE.equals(updateVehicleRequest.getIsPrimaryVehicle())) {
            List<VehicleDetails> allVehicles = vehicleDetailsRepository.findAllByDeliveryPartnerId(deliveryPartner.getId());
            for (VehicleDetails v : allVehicles) {
                v.setIsPrimaryVehicle(false);
            }
            vehicleDetails.setIsPrimaryVehicle(true);
            vehicleDetailsRepository.saveAll(allVehicles); // save all
        }

        vehicleDetailsRepository.save(vehicleDetails);

        return DeliveryPartnerResponse.builder()
                .responseCode(DeliveryPartnerUtils.VEHICLE_UPDATE_CODE)
                .success(DeliveryPartnerUtils.VEHICLE_UPDATE_SUCCESS)
                .responseMessage(DeliveryPartnerUtils.VEHICLE_UPDATE_MESSAGE)
                .build();
    }

    @Override
    public DeliveryPartnerResponse deleteVehicle(Long vehicleId, String email) {
        /**
         * Delete a vehicle
         */

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        VehicleDetails vehicleDetails = vehicleDetailsRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicleDetails.getDeliveryPartner().getId().equals(deliveryPartner.getId())) {
            throw new RuntimeException("You are not authorized to update this vehicle.");
        }

        boolean wasPrimary = Boolean.TRUE.equals(vehicleDetails.getIsPrimaryVehicle());

        // Delete the vehicle
        vehicleDetailsRepository.delete(vehicleDetails);

        // If the deleted vehicle was primary, assign another one as primary
        if (wasPrimary) {
            List<VehicleDetails> remainingVehicles = vehicleDetailsRepository.findAllByDeliveryPartnerId(deliveryPartner.getId());

            if (!remainingVehicles.isEmpty()) {
                // Just assign the first one as the new primary
                VehicleDetails newPrimary = remainingVehicles.get(0);
                newPrimary.setIsPrimaryVehicle(true);
                vehicleDetailsRepository.save(newPrimary);
            }
        }

        return DeliveryPartnerResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Vehicle deleted successfully.")
                .build();
    }
}