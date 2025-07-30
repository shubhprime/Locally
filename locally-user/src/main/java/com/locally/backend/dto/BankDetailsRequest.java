package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDetailsRequest {
    private String email;
    private String accountHolderName;
    private String bankName;
    private String routingNumber;
    private String bankAccountNumber;
    private String accountType;
}