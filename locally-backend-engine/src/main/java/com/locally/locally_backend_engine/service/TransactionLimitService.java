package com.locally.locally_backend_engine.service;

import java.math.BigDecimal;

public interface TransactionLimitService {
    void validateTransactionLimits(Long userId, BigDecimal amount);
}