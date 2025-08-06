package com.locally.locally_delivery_partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDetailsResponse {
    private String accountHolderName;
    private String bankName;
    private String routingNumber;
    private String bankAccountNumber;
    private String accountType;
    private boolean hasBankDetails;
}