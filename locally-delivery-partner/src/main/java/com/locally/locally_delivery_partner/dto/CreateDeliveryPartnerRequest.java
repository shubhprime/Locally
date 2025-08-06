package com.locally.locally_delivery_partner.dto;

import com.locally.locally_delivery_partner.model.ModeOfDelivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDeliveryPartnerRequest {

    private String firstName;
    private String lastName;
    private String gender;
    private String state;
    private String country;
    private String address;
    private String email;
    private String phoneNumber;
    private String alternatePhoneNumber;
    private ModeOfDelivery modeOfDelivery;
    private String governmentIdType;
    private String governmentId;
    private String accountHolderName;
    private String bankName;
    private String routingNumber;
    private String bankAccountNumber;
    private String accountType;
    private String password;
}