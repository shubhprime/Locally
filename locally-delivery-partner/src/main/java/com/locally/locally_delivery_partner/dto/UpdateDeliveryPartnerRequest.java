package com.locally.locally_delivery_partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDeliveryPartnerRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private String state;
    private String country;
    private String address;
    private String phoneNumber;
    private String alternatePhoneNumber;
    private String governmentIdType;
    private String governmentId;
    private String bankAccountNumber;
}