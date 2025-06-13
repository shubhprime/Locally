package com.locally.locally_delivery_partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVehicleRequest {
    private String email;
    private Long vehicleId;
    private String vehicleType;
    private String vehicleColor;
    private String vehicleModel;
    private String registrationNumber;
    private String licenseNumber;
    private Boolean isPrimaryVehicle;
}