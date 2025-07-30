package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalResponse {
    private String transactionId;
    private String status; // "SUCCESS", "PENDING", "FAILED"
    private Double amount;
    private String estimatedArrival; // "1-3 business days"
    private String message;
}