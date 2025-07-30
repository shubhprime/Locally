package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private String state;
    private String country;
    private String address;
    private String phoneNumber;
    private String alternatePhoneNumber;
    private String accountHolderName;
    private String bankName;
    private String routingNumber;
    private String bankAccountNumber;
    private String accountType;
}