package com.locally.locally_backend_engine.service;

import com.locally.locally_backend_engine.dto.TransactionDTO;
import com.locally.locally_backend_engine.model.TransactionStatus;
import com.locally.locally_backend_engine.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Optional<TransactionDTO> getTransactionByTransactionId(String transactionId);

    Page<TransactionDTO> getTransactionsByType(Long userId, TransactionType type, Pageable pageable);

    Page<TransactionDTO> getTransactionsByDateRange(Long userId, LocalDateTime startDate,
                                                    LocalDateTime endDate, Pageable pageable);

    List<TransactionDTO> getPendingTransactions(Long userId);

    Page<TransactionDTO> getPendingTransactions(Long userId, Pageable pageable);

    long getTransactionCountByStatus(Long userId, TransactionStatus status);
}