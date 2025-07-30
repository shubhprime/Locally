package com.locally.locally_backend_engine.service;

import com.locally.locally_backend_engine.dto.*;
import org.springframework.data.domain.Pageable;

public interface WalletService {
    WalletDTO createWallet(Long userId);

    WalletDTO getWalletByUserId(Long userId);

    TransactionResponse addMoney(AddMoneyRequest request);

    void processSuccessfulPayment(String paymentIntentId);

    TransactionResponse withdrawMoney(WithdrawMoneyRequest withdrawMoneyRequest);

    TransactionHistoryDTO getTransactionHistory(Long userId, Pageable pageable);
}