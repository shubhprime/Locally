package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.*;

public interface WalletService {
    AppResponse<WalletDTO> createWallet(String email);

    AppResponse<WalletDTO> getWalletByUserId(String email);

    AppResponse<TransactionResponse> withdrawMoney(WithdrawMoneyRequest withdrawMoneyRequest);

    AppResponse<TransactionHistoryDTO> getTransactionHistory(String email, int page, int size);
}