package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {
    private String label;
    private String fullAddress;
    private String houseUnit;
    private String instructions;
    private Boolean isDefault;
}