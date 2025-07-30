package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String transactionId;
    private String paymentIntentId;
    private String clientSecret;
    private String status;
    private double amount;
    private String currency;
    private boolean requiresAction;
    private String nextActionType;
    private String bankAccountNumber;
    private String routingNumber;
    private String accountName;
    private String referenceNumber;
    private String instructions;
    private String errorCode;
    private String errorMessage;
    private String createdAt;
    private String updatedAt;
}