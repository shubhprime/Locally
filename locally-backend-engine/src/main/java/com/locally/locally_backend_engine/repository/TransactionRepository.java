package com.locally.locally_backend_engine.repository;

import com.locally.locally_backend_engine.model.Transaction;
import com.locally.locally_backend_engine.model.TransactionStatus;
import com.locally.locally_backend_engine.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<Transaction> findByReferenceId(String referenceId);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
            "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:fromDate IS NULL OR t.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR t.createdAt <= :toDate) " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findTransactionHistory(
            @Param("userId") Long userId,
            @Param("transactionType") TransactionType transactionType,
            @Param("status") TransactionStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.type = :type ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndType(@Param("userId") Long userId,
                                          @Param("type") TransactionType type,
                                          Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TransactionStatus status);

    Page<Transaction> findByUserIdAndStatus(Long userId, TransactionStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND DATE(t.createdAt) = :date " +
            "AND t.status = 'COMPLETED' " +
            "AND t.transactionType IN ('ADD_MONEY', 'PAYMENT')")
    BigDecimal getDailyTransactionSum(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.createdAt >= :since " +
            "AND t.status IN ('COMPLETED', 'PENDING')")
    int countTransactionsSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.createdAt >= :since " +
            "AND t.amount >= :threshold " +
            "AND t.status = 'COMPLETED'")
    BigDecimal getHighValueTransactionSum(@Param("userId") Long userId,
                                          @Param("since") LocalDateTime since,
                                          @Param("threshold") BigDecimal threshold);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.transactionType = :type " +
            "AND t.createdAt >= :startDate " +
            "AND t.createdAt <= :endDate " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndType(@Param("userId") Long userId,
                                          @Param("type") TransactionType type,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);
}