package com.locally.locally_delivery_partner.dto;

import com.locally.locally_delivery_partner.model.TransactionStatus;
import com.locally.locally_delivery_partner.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private Long walletId;
    private Long deliveryPartnerId;
    private String transactionId;
    private BigDecimal amount;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String stripePaymentIntentId;
    private String stripeChargeId;
    private String referenceId;
    private String description;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}