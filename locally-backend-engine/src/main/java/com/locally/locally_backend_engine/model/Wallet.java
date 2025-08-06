package com.locally.locally_backend_engine.model;

import com.locally.locally_backend_engine.decoder.ignore404Error.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallet_user_id", columnList = "user_id")
})
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Builder.Default
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "frozen_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal frozenBalance = BigDecimal.ZERO;

//    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_frozen", nullable = false)
    private Boolean isFrozen = false;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to get available balance
    public BigDecimal getAvailableBalance() {
        return balance.subtract(frozenBalance);
    }

    // Helper method to check if wallet can perform transaction
    public boolean canPerformTransaction(BigDecimal amount) {
        return isActive && getAvailableBalance().compareTo(amount) >= 0;
    }
}