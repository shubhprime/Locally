package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.config.WalletConfig;
import com.locally.locally_backend_engine.exception.DailyLimitExceededException;
import com.locally.locally_backend_engine.exception.FraudDetectedException;
import com.locally.locally_backend_engine.exception.TransactionLimitExceededException;
import com.locally.locally_backend_engine.repository.TransactionRepository;
import com.locally.locally_backend_engine.service.TransactionLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
public class TransactionLimitServiceImpl implements TransactionLimitService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletConfig walletConfig;

    @Override
    public void validateTransactionLimits(Long userId, BigDecimal amount) {

        // 1. Validate basic amount limits
        if (!walletConfig.isValidTransactionAmount(amount)) {
            throw new TransactionLimitExceededException(
                    String.format("Transaction amount %.2f is outside allowed limits (%.2f - %.2f)",
                            amount, walletConfig.getMinTransactionAmount(), walletConfig.getMaxTransactionAmount())
            );
        }

        // 2. Check daily transaction limits
        validateDailyLimits(userId, amount);

        // 3. Check hourly transaction frequency
        validateHourlyFrequency(userId);

        // 4. Check for suspicious amounts
        validateSuspiciousActivity(userId, amount);

        // 5. Check for rapid successive transactions (velocity check)
        validateTransactionVelocity(userId);
    }

    private void validateDailyLimits(Long userId, BigDecimal amount) {
        LocalDate today = LocalDate.now();
        BigDecimal dailyTotal = transactionRepository.getDailyTransactionSum(userId, today);

        if (dailyTotal == null) dailyTotal = BigDecimal.ZERO;

        BigDecimal newDailyTotal = dailyTotal.add(amount);

        if (newDailyTotal.compareTo(walletConfig.getMaxDailyTransactionLimit()) > 0) {
            log.warn("Daily limit exceeded for user {}: current={}, attempted={}, limit={}",
                    userId, dailyTotal, amount, walletConfig.getMaxDailyTransactionLimit());

            throw new DailyLimitExceededException(
                    String.format("Daily transaction limit of %.2f exceeded. Current total: %.2f",
                            walletConfig.getMaxDailyTransactionLimit(), dailyTotal)
            );
        }
    }

    private void validateHourlyFrequency(Long userId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        int hourlyTransactionCount = transactionRepository.countTransactionsSince(userId, oneHourAgo);

        if (hourlyTransactionCount >= walletConfig.getMaxTransactionsPerHour()) {
            log.warn("Hourly frequency limit exceeded for user {}: count={}, limit={}",
                    userId, hourlyTransactionCount, walletConfig.getMaxTransactionsPerHour());

            throw new TransactionLimitExceededException(
                    String.format("Too many transactions in the last hour. Limit: %d per hour",
                            walletConfig.getMaxTransactionsPerHour())
            );
        }
    }

    private void validateSuspiciousActivity(Long userId, BigDecimal amount) {
        // Check for suspicious amounts
        if (walletConfig.isAmountSuspicious(amount)) {
            log.warn("Suspicious amount detected for user {}: amount={}", userId, amount);

            // For now, just log - in production you might want to require additional verification
            // throw new FraudDetectedException("Transaction requires additional verification");
        }

        // Check for multiple high-value transactions in short time
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        BigDecimal recentHighValueTotal = transactionRepository.getHighValueTransactionSum(
                userId, oneHourAgo, walletConfig.getSuspiciousAmountThreshold()
        );

        if (recentHighValueTotal != null &&
                recentHighValueTotal.add(amount).compareTo(walletConfig.getSuspiciousAmountThreshold().multiply(BigDecimal.valueOf(2))) > 0) {

            log.error("Multiple high-value transactions detected for user {}: total={}",
                    userId, recentHighValueTotal.add(amount));

            throw new FraudDetectedException("Multiple high-value transactions detected. Please contact support.");
        }
    }

    private void validateTransactionVelocity(Long userId) {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        int recentTransactionCount = transactionRepository.countTransactionsSince(userId, fiveMinutesAgo);

        // Maximum 5 transactions in 5 minutes
        if (recentTransactionCount >= 5) {
            log.warn("High transaction velocity detected for user {}: {} transactions in 5 minutes",
                    userId, recentTransactionCount);

            throw new TransactionLimitExceededException(
                    "Too many transactions in a short period. Please wait before making another transaction."
            );
        }
    }
}