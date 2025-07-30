package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.dto.TransactionDTO;
import com.locally.locally_backend_engine.model.Transaction;
import com.locally.locally_backend_engine.model.TransactionStatus;
import com.locally.locally_backend_engine.model.TransactionType;
import com.locally.locally_backend_engine.repository.TransactionRepository;
import com.locally.locally_backend_engine.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public Optional<TransactionDTO> getTransactionByTransactionId(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .map(this::convertToDto);
    }

    @Override
    public Page<TransactionDTO> getTransactionsByType(Long userId, TransactionType type, Pageable pageable) {
        return transactionRepository.findByUserIdAndType(userId, type, pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<TransactionDTO> getTransactionsByDateRange(Long userId, LocalDateTime startDate,
                                                           LocalDateTime endDate, Pageable pageable) {
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable)
                .map(this::convertToDto);
    }

    @Override
    public List<TransactionDTO> getPendingTransactions(Long userId) {
        Pageable pageable = PageRequest.of(0, 25);
        return getPendingTransactions(userId, pageable).getContent();
    }

    @Override
    public Page<TransactionDTO> getPendingTransactions(Long userId, Pageable pageable) {
        // Add pagination to prevent memory issues
        return transactionRepository.findByUserIdAndStatus(userId, TransactionStatus.PENDING, pageable)
                .map(this::convertToDto);
    }

    @Override
    public long getTransactionCountByStatus(Long userId, TransactionStatus status) {
        return transactionRepository.countByUserIdAndStatus(userId, status);
    }

    private TransactionDTO convertToDto(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUserId());
        dto.setWalletId(transaction.getWalletId());
        dto.setTransactionId(transaction.getTransactionId());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setPreviousBalance(transaction.getPreviousBalance());
        dto.setNewBalance(transaction.getNewBalance());
        dto.setStatus(transaction.getStatus());
        dto.setStripePaymentIntentId(transaction.getStripePaymentIntentId());
        dto.setStripeChargeId(transaction.getStripeChargeId());
        dto.setDescription(transaction.getDescription());
        dto.setFailureReason(transaction.getFailureReason());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setProcessedAt(transaction.getProcessedAt());
        return dto;
    }
}