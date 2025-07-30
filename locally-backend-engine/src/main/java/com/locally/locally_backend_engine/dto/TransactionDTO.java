package com.locally.locally_backend_engine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.locally.locally_backend_engine.model.PaymentMethod;
import com.locally.locally_backend_engine.model.TransactionStatus;
import com.locally.locally_backend_engine.model.TransactionType;
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
    private Long userId;
    private String transactionId;
    private BigDecimal amount;
    private String stripePaymentIntentId;
    private String stripeChargeId;

    @JsonProperty("currency")
    private String currency;

    private PaymentMethod paymentMethodType;

    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String referenceId;
    private String description;
    private String failureReason;

    @JsonProperty("is_processed")
    private Boolean isProcessed;

    @JsonProperty("is_expired")
    private Boolean isExpired;

    @JsonProperty("retry_count")
    private Integer retryCount;

    @JsonProperty("webhook_processed")
    private Boolean webhookProcessed;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("processed_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;

    @JsonProperty("expires_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
}