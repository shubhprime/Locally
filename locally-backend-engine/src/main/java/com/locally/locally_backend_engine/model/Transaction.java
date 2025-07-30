package com.locally.locally_backend_engine.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_user_id_created_at", columnList = "user_id, created_at DESC"),
        @Index(name = "idx_transaction_stripe_payment_intent_id", columnList = "stripe_payment_intent_id"),
        @Index(name = "idx_transaction_idempotency_key", columnList = "idempotency_key"),
        @Index(name = "idx_transaction_user_id_status", columnList = "user_id, status"),
        @Index(name = "idx_transaction_expires_at", columnList = "expires_at")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "previous_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal previousBalance;

    @Column(name = "new_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal newBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method_type")
    private PaymentMethod paymentMethodType;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "description")
    private String description;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "is_processed", nullable = false)
    private Boolean isProcessed = false;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "webhook_processed", nullable = false)
    private Boolean webhookProcessed = false;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Helper method to check if transaction is final
    public boolean isFinalStatus() {
        return status == TransactionStatus.COMPLETED ||
                status == TransactionStatus.FAILED ||
                status == TransactionStatus.CANCELLED;
    }

    // Helper method to check if transaction can be processed
    public boolean canBeProcessed() {
        return !isProcessed && !isFinalStatus() &&
                (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }

    // Helper method to check if transaction is expired
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    // Helper method to increment retry count
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount != null ? this.retryCount : 0) + 1;
    }

    // Helper method to mark as processed
    public void markAsProcessed() {
        this.isProcessed = true;
        this.processedAt = LocalDateTime.now();
    }
}