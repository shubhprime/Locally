package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.config.UserLockManager;
import com.locally.locally_backend_engine.config.WalletConfig;
import com.locally.locally_backend_engine.dto.*;
import com.locally.locally_backend_engine.exception.DuplicateTransactionException;
import com.locally.locally_backend_engine.exception.InsufficientBalanceException;
import com.locally.locally_backend_engine.exception.WalletNotFoundException;
import com.locally.locally_backend_engine.exception.WalletOperationException;
import com.locally.locally_backend_engine.model.Transaction;
import com.locally.locally_backend_engine.model.TransactionStatus;
import com.locally.locally_backend_engine.model.TransactionType;
import com.locally.locally_backend_engine.model.Wallet;
import com.locally.locally_backend_engine.repository.TransactionRepository;
import com.locally.locally_backend_engine.repository.WalletRepository;
import com.locally.locally_backend_engine.service.TransactionLimitService;
import com.locally.locally_backend_engine.service.WalletService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private TransactionLimitService transactionLimitService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletConfig walletConfig;

    @Autowired
    private UserLockManager lockManager;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletDTO createWallet(Long userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Wallet already exists for user: " + userId);
        }

        try {
            // Create Stripe customer
            Map<String, Object> customerParams = new HashMap<>();
            customerParams.put("metadata", Map.of("userId", userId.toString()));
            Customer customer = Customer.create(customerParams);

            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .balance(BigDecimal.ZERO)
                    .frozenBalance(BigDecimal.ZERO)
                    .stripeCustomerId(customer.getId())
                    .currency("USD")
                    .isActive(true)
                    .isFrozen(false)
                    .build();

            Wallet savedWallet = walletRepository.save(wallet);
            log.info("Created wallet for user {}: walletId={}", userId, savedWallet.getId());

            return convertToDto(savedWallet);

        } catch (StripeException e) {
            log.error("Error creating Stripe customer for user {}: {}", userId, e.getMessage());
            throw new WalletOperationException(
                    "Unable to create wallet. Please try again.",
                    "Failed to create Stripe customer for user " + userId,
                    e
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WalletDTO getWalletByUserId(Long userId) {
        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);

        if (walletOpt.isEmpty()) {
            log.info("Wallet not found for user: {}", userId);
            return null; // Return null when wallet doesn't exist
        }

        return convertToDto(walletOpt.get());
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class, propagation = Propagation.REQUIRED, timeout = 7)
    public TransactionResponse addMoney(AddMoneyRequest addMoneyRequest) {

        // Get user-specific lock to prevent race conditions
        ReentrantLock userLock = userLocks.computeIfAbsent(addMoneyRequest.getUserId(), k -> new ReentrantLock());

        try {
            userLock.lock();

            transactionLimitService.validateTransactionLimits(addMoneyRequest.getUserId(), addMoneyRequest.getAmount());

            validateAddMoneyRequest(addMoneyRequest);

            // Generate idempotency key for duplicate prevention
            String idempotencyKey = generateIdempotencyKey(addMoneyRequest);

            // Check for existing transaction with same idempotency key
            Optional<Transaction> existingTransaction =
                    transactionRepository.findByIdempotencyKey(idempotencyKey);

            if (existingTransaction.isPresent()) {
                Transaction existing = existingTransaction.get();
                log.info("Duplicate add money request detected for user: {}", addMoneyRequest.getUserId());
                return TransactionResponse.builder()
                        .transactionId(existing.getTransactionId())
                        .status(existing.getStatus().name().toLowerCase())
                        .description("Transaction already exists")
                        .build();
            }

            Wallet wallet = walletRepository.findByUserIdWithLock(addMoneyRequest.getUserId())
                    .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + addMoneyRequest.getUserId()));

            if (!wallet.getIsActive()) {
                throw new IllegalStateException("Wallet is not active");
            }

            String transactionId = generateTransactionId();

            // Create Stripe PaymentIntent
            Map<String, Object> params = new HashMap<>();
            params.put("amount", addMoneyRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue()); // Convert to cents
            params.put("currency", wallet.getCurrency().toLowerCase());
            params.put("customer", wallet.getStripeCustomerId());

            if (addMoneyRequest.getPaymentMethod() != null) {
                params.put("payment_method", addMoneyRequest.getPaymentMethod());
                params.put("confirm", true);
            }

            Map<String, String> metadata = new HashMap<>();
            metadata.put("transactionId", transactionId);
            metadata.put("userId", addMoneyRequest.getUserId().toString());
            metadata.put("idempotencyKey", idempotencyKey);
            params.put("metadata", metadata);

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Create transaction record
            Transaction transaction = Transaction.builder()
                    .userId(addMoneyRequest.getUserId())
                    .walletId(wallet.getId())
                    .transactionId(transactionId)
                    .transactionType(TransactionType.ADD_MONEY)
                    .amount(addMoneyRequest.getAmount())
                    .previousBalance(wallet.getBalance())
                    .newBalance(wallet.getBalance()) // Will be updated when payment succeeds
                    .status(TransactionStatus.PENDING)
                    .stripePaymentIntentId(paymentIntent.getId())
                    .paymentMethodType(addMoneyRequest.getPaymentMethod())
                    .description(addMoneyRequest.getDescription())
                    .currency(wallet.getCurrency())
                    .idempotencyKey(idempotencyKey)
                    .isProcessed(false)
                    .webhookProcessed(false)
                    .expiresAt(LocalDateTime.now().plusHours(walletConfig.getPaymentIntentExpiryHours())) // Payment intent expires in 24 hours
                    .build();

            transactionRepository.save(transaction);

            log.info("Created add money transaction for user {}: transactionId={}, amount={}",
                    addMoneyRequest.getUserId(), transactionId, addMoneyRequest.getAmount());

            return TransactionResponse.builder()
                    .transactionId(transactionId)
                    .status(paymentIntent.getStatus())
                    .description("Payment initiated successfully")
                    .clientSecret(paymentIntent.getClientSecret())
                    .build();

        } catch (StripeException e) {
            log.error("Error processing add money for user {}: {}", addMoneyRequest.getUserId(), e.getMessage(), e);
            throw new WalletOperationException(
                    "Payment initiation failed. Please check your payment method.",
                    "Stripe PaymentIntent creation failed for user " + addMoneyRequest.getUserId(),
                    e
            );
        } finally {
            userLock.unlock();
            // Clean up lock if no other threads are waiting
            lockManager.releaseUserLock(addMoneyRequest.getUserId());
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void processSuccessfulPayment(String paymentIntentId) {
        log.info("Processing successful payment for PaymentIntent: {}", paymentIntentId);

        Optional<Transaction> transactionOpt = transactionRepository.findByStripePaymentIntentId(paymentIntentId);

        if (transactionOpt.isEmpty()) {
            log.error("Transaction not found for PaymentIntent: {}", paymentIntentId);
            return;
        }

        Transaction transaction = transactionOpt.get();
        Long userId = transaction.getUserId();

        // Get user lock to prevent concurrent processing
        ReentrantLock userLock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());

        try {
            userLock.lock();

            // Double-check transaction state after acquiring lock
            transaction = transactionRepository.findById(transaction.getId()).orElse(transaction);

            // Check if already processed to prevent duplicate processing
            if (transaction.getIsProcessed() || transaction.getWebhookProcessed()) {
                log.info("Transaction {} already processed, skipping", transaction.getTransactionId());
                return;
            }

            // Check if transaction is in a final state
            if (transaction.isFinalStatus()) {
                log.info("Transaction {} is already in final state: {}",
                        transaction.getTransactionId(), transaction.getStatus());
                return;
            }

            if (transaction.isExpired()) {
                log.warn("Transaction {} is expired, marking as failed", transaction.getTransactionId());
                markTransactionAsFailed(transaction, "Transaction expired");
                return;
            }

            Wallet wallet = walletRepository.findByUserIdWithLock(transaction.getUserId())
                    .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

            // Update wallet balance
            BigDecimal previousBalance = wallet.getBalance();
            BigDecimal newBalance = wallet.getBalance().add(transaction.getAmount());

            wallet.setBalance(newBalance);
            wallet.setLastTransactionAt(LocalDateTime.now());

            // Update transaction
            transaction.setNewBalance(newBalance);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.markAsProcessed();
            transaction.setWebhookProcessed(true);

            walletRepository.save(wallet);
            transactionRepository.save(transaction);

            log.info("Payment processed successfully for transaction: {}, userId={}, amount={}, newBalance={}", transaction.getTransactionId(), userId, transaction.getAmount(), newBalance);
        } catch (Exception e) {
            log.error("Error processing successful payment for transaction {}: {}",
                    transaction.getTransactionId(), e.getMessage(), e);

            // Mark transaction as failed
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Internal processing error: " + e.getMessage());
            transaction.setProcessedAt(LocalDateTime.now());
            transaction.setWebhookProcessed(true);
            transactionRepository.save(transaction);

            throw e;
        } finally {
            userLock.unlock();
            if (!userLock.hasQueuedThreads()) {
                userLocks.remove(userId);
            }
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public TransactionResponse withdrawMoney(WithdrawMoneyRequest withdrawMoneyRequest) {
        ReentrantLock userLock = userLocks.computeIfAbsent(withdrawMoneyRequest.getUserId(), k -> new ReentrantLock());

        try {
            userLock.lock();

            transactionLimitService.validateTransactionLimits(withdrawMoneyRequest.getUserId(), withdrawMoneyRequest.getAmount());

            // Generate idempotency key for duplicate prevention
            String idempotencyKey = generateIdempotencyKey(withdrawMoneyRequest);

            // Check for existing transaction with same idempotency key
            Optional<Transaction> existingTransaction =
                    transactionRepository.findByIdempotencyKey(idempotencyKey);

            if (existingTransaction.isPresent()) {
                throw new DuplicateTransactionException("Duplicate withdrawal request");
            }

            Wallet wallet = walletRepository.findByUserIdWithLock(withdrawMoneyRequest.getUserId())
                    .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + withdrawMoneyRequest.getUserId()));

            if (!wallet.canPerformTransaction(withdrawMoneyRequest.getAmount())) {
                if (!wallet.getIsActive()) {
                    throw new IllegalStateException("Wallet is not active");
                }
                if (wallet.getIsFrozen()) {
                    throw new IllegalStateException("Wallet is frozen");
                }
                throw new InsufficientBalanceException("Insufficient balance for withdrawal");
            }

            BigDecimal totalDeduction = withdrawMoneyRequest.getAmount();

            if (wallet.getBalance().compareTo(totalDeduction) < 0) {
                throw new InsufficientBalanceException(
                        String.format("Insufficient balance for withdrawal. Available: %.2f, Required: %.2f (amount: %.2f)",
                                wallet.getAvailableBalance(), totalDeduction, withdrawMoneyRequest.getAmount())
                );
            }

            String transactionId = generateTransactionId();

            // Update wallet balance
            BigDecimal previousBalance = wallet.getBalance();
            BigDecimal newBalance = previousBalance.subtract(withdrawMoneyRequest.getAmount());

            // Create transaction record
            Transaction transaction = Transaction.builder()
                    .userId(withdrawMoneyRequest.getUserId())
                    .walletId(wallet.getId())
                    .transactionId(transactionId)
                    .transactionType(TransactionType.PAYMENT)
                    .amount(withdrawMoneyRequest.getAmount())
                    .previousBalance(previousBalance)
                    .newBalance(newBalance)
                    .status(TransactionStatus.COMPLETED)
                    .description(withdrawMoneyRequest.getDescription())
                    .currency(wallet.getCurrency())
                    .idempotencyKey(idempotencyKey)
                    .isProcessed(true)
                    .processedAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(transaction);

            // Update wallet balance after transaction is saved
            wallet.setBalance(newBalance);
            wallet.setLastTransactionAt(LocalDateTime.now());
            walletRepository.save(wallet);

            log.info("Withdrawal processed successfully: transactionId={}, userId={}, amount={}, newBalance={}",
                    transactionId, withdrawMoneyRequest.getUserId(), withdrawMoneyRequest.getAmount(), newBalance);

            return TransactionResponse.builder()
                    .transactionId(transactionId)
                    .status("succeeded")
                    .description("Withdrawal completed successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error processing withdrawal for user {}: {}", withdrawMoneyRequest.getUserId(), e.getMessage());
            throw new WalletOperationException(
                    "Withdrawal failed. Please try again.",
                    "Withdrawal processing error for user " + withdrawMoneyRequest.getUserId(),
                    e
            );
        } finally {
            userLock.unlock();
            lockManager.releaseUserLock(withdrawMoneyRequest.getUserId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionHistoryDTO getTransactionHistory(Long userId, Pageable pageable) {
        Page<Transaction> transactionPage = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        TransactionHistoryDTO historyDto = new TransactionHistoryDTO();
        historyDto.setTransactions(transactionPage.getContent().stream()
                .map(this::convertTransactionToDto)
                .toList());
        historyDto.setTotalPages(transactionPage.getTotalPages());
        historyDto.setTotalElements(transactionPage.getTotalElements());
        historyDto.setCurrentPage(transactionPage.getNumber());
        historyDto.setPageSize(transactionPage.getSize());

        return historyDto;
    }

    private void markTransactionAsFailed(Transaction transaction, String reason) {
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailureReason(reason);
        transaction.markAsProcessed();
        transaction.setWebhookProcessed(true);
        transactionRepository.save(transaction);
    }

    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private void validateAddMoneyRequest(AddMoneyRequest request) {
        if (request.getAmount().compareTo(walletConfig.getMinTransactionAmount()) < 0) {
            throw new IllegalArgumentException("Amount is below minimum limit");
        }
        if (request.getAmount().compareTo(walletConfig.getMaxTransactionAmount()) > 0) {
            throw new IllegalArgumentException("Amount exceeds maximum limit");
        }
    }

    private void validateWithdrawMoneyRequest(WithdrawMoneyRequest request) {
        if (request.getAmount().compareTo(walletConfig.getMinTransactionAmount()) < 0) {
            throw new IllegalArgumentException("Amount is below minimum limit");
        }
        if (request.getAmount().compareTo(walletConfig.getMaxTransactionAmount()) > 0) {
            throw new IllegalArgumentException("Amount exceeds maximum limit");
        }
    }

    private PaymentIntent createStripePaymentIntent(AddMoneyRequest request, Wallet wallet,
                                                    String transactionId, String idempotencyKey, BigDecimal totalAmount) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).longValue()); // Convert to cents
        params.put("currency", wallet.getCurrency().toLowerCase());
        params.put("customer", wallet.getStripeCustomerId());

        if (request.getPaymentMethod() != null) {
            params.put("payment_method", request.getPaymentMethod());
            params.put("confirm", true);
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("transactionId", transactionId);
        metadata.put("userId", request.getUserId().toString());
        metadata.put("idempotencyKey", idempotencyKey);
        metadata.put("amount", request.getAmount().toString());
        params.put("metadata", metadata);

        return PaymentIntent.create(params);
    }

    private WalletDTO convertToDto(Wallet wallet) {
        WalletDTO dto = new WalletDTO();
        dto.setId(wallet.getId());
        dto.setUserId(wallet.getUserId());
        dto.setBalance(wallet.getBalance());
        dto.setCurrency(wallet.getCurrency());
        dto.setIsActive(wallet.getIsActive());
        dto.setIsFrozen(wallet.getIsFrozen());
        dto.setLastTransactionAt(wallet.getLastTransactionAt());
        dto.setCreatedAt(wallet.getCreatedAt());
        dto.setUpdatedAt(wallet.getUpdatedAt());
        return dto;
    }

    private TransactionDTO convertTransactionToDto(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUserId());
        dto.setWalletId(transaction.getWalletId());
        dto.setTransactionId(transaction.getTransactionId());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setPreviousBalance(transaction.getPreviousBalance());
        dto.setNewBalance(transaction.getNewBalance());
        dto.setStatus(transaction.getStatus());
        dto.setPaymentMethodType(transaction.getPaymentMethodType());
        dto.setDescription(transaction.getDescription());
        dto.setFailureReason(transaction.getFailureReason());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setProcessedAt(transaction.getProcessedAt());
        dto.setRetryCount(transaction.getRetryCount());
        dto.setIsExpired(transaction.isExpired());
        return dto;
    }

    private String generateIdempotencyKey(AddMoneyRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("ADD_")
                .append(request.getUserId())
                .append("_")
                .append(request.getAmount().toString())
                .append("_");

        if (request.getPaymentMethod() != null) {
            keyBuilder.append(request.getPaymentMethod()).append("_");
        }

        if (request.getDescription() != null) {
            keyBuilder.append(request.getDescription().hashCode()).append("_");
        }

        keyBuilder.append(System.currentTimeMillis() / 1000);

        return keyBuilder.toString().replaceAll("[^a-zA-Z0-9_]", "");
    }

    private String generateIdempotencyKey(WithdrawMoneyRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("WITHDRAW_")
                .append(request.getUserId())
                .append("_")
                .append(request.getAmount().toString())
                .append("_");

        if (request.getDescription() != null) {
            keyBuilder.append(request.getDescription().hashCode()).append("_");
        }

        keyBuilder.append(System.currentTimeMillis() / 1000);

        return keyBuilder.toString().replaceAll("[^a-zA-Z0-9_]", "");
    }

    // Cleanup method for user locks - call this periodically
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupUserLocks() {
        userLocks.entrySet().removeIf(entry ->
                !entry.getValue().hasQueuedThreads() && !entry.getValue().isLocked());

        log.debug("Cleaned up user locks, remaining: {}", userLocks.size());
    }
}