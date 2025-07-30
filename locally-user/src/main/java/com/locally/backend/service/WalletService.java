package com.locally.backend.service;

import com.locally.backend.dto.*;

public interface WalletService {
    AppResponse<WalletDTO> createWallet(String email);

    AppResponse<WalletDTO> getWalletByUserId(String email);

    AppResponse<TransactionResponse> addMoney(AddMoneyRequest addMoneyRequest);

    AppResponse<TransactionResponse> withdrawMoney(WithdrawMoneyRequest withdrawMoneyRequest);

    AppResponse<TransactionHistoryDTO> getTransactionHistory(String email, int page, int size);
}