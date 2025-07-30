package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String type; // "ADD_MONEY", "WITHDRAWAL", "EARNING", "SPENDING", "REFUND"
    private Double amount;
    private String clientSecret;
    private String description;
    private LocalDateTime date;
    private String status; // "PENDING", "COMPLETED", "FAILED"
    private String paymentMethod;
}