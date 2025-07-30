package com.locally.locally_backend_engine.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "wallet")
@Validated
public class WalletConfig {

    @NotNull
    @Min(1)
    private Integer paymentIntentExpiryHours = 24;

    @NotNull
    @Min(1)
    private Integer maxRetryAttempts = 3;

    @NotNull
    private BigDecimal minTransactionAmount = new BigDecimal("0.01");

    @NotNull
    private BigDecimal maxTransactionAmount = new BigDecimal("10000.00");

    @NotNull
    private BigDecimal maxDailyTransactionLimit = new BigDecimal("50000.00");

    @NotNull
    private String defaultCurrency = "USD";

    @NotNull
    @Min(1)
    private Integer transactionHistoryDays = 90;

    // Webhook retry configuration
    @NotNull
    @Min(1)
    private Integer webhookMaxRetries = 5;

    @NotNull
    @Min(1000)
    private Long webhookRetryDelayMs = 2000L;

    // Stripe configuration
    @NotNull
    private String stripeSuccessUrl = "https://your-app.com/payment/success";

    @NotNull
    private String stripeCancelUrl = "https://your-app.com/payment/cancel";

    // Fee configuration
    private BigDecimal transactionFeePercentage = new BigDecimal("0.029"); // 2.9%
    private BigDecimal fixedTransactionFee = new BigDecimal("0.30"); // $0.30

    // Wallet limits
    private BigDecimal maxWalletBalance = new BigDecimal("100000.00");
    private BigDecimal minWalletBalance = BigDecimal.ZERO;

    // Fraud detection
    private Integer maxTransactionsPerHour = 50;
    private BigDecimal suspiciousAmountThreshold = new BigDecimal("5000.00");

    public BigDecimal calculateTransactionFee(BigDecimal amount) {
        BigDecimal percentageFee = amount.multiply(transactionFeePercentage);
        return percentageFee.add(fixedTransactionFee);
    }

    public boolean isAmountSuspicious(BigDecimal amount) {
        return amount.compareTo(suspiciousAmountThreshold) >= 0;
    }

    public boolean isValidTransactionAmount(BigDecimal amount) {
        return amount.compareTo(minTransactionAmount) >= 0 &&
                amount.compareTo(maxTransactionAmount) <= 0;
    }
}