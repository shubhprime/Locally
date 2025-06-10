package com.locally.locally_delivery_partner.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVehicleRequest {
    @JsonIgnore
    private String email;
    private String vehicleType;
    private String vehicleColor;
    private String vehicleModel;
    private String registrationNumber;
    private String licenseNumber;
}