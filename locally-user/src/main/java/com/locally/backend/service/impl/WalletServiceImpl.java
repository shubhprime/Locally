package com.locally.backend.service.impl;

import com.locally.backend.client.EngineWalletClient.EngineWalletClient;
import com.locally.backend.dto.*;
import com.locally.backend.model.User;
import com.locally.backend.repository.UserRepository;
import com.locally.backend.service.WalletService;
import com.locally.backend.utils.AccountUtils;
import com.locally.backend.utils.JwtUtil;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EngineWalletClient engineWalletClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AppResponse<WalletDTO> createWallet(String email) {
        try {
            User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            log.info("Creating wallet for user: {}", email);

            // Generate service token
            String serviceToken = jwtUtil.generateServiceToken(user.getId());

            WalletDTO walletDTO = engineWalletClient.createWallet("Bearer " + serviceToken).getBody();

            return AppResponse.<WalletDTO>builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage("Wallet created successfully")
                    .data(walletDTO)
                    .build();
        } catch (FeignException e) {
            log.error("Error creating wallet for user {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to create wallet: " + e.getMessage());
        }
    }

    @Override
    public AppResponse<WalletDTO> getWalletByUserId(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            log.info("Fetching wallet for user: {}", email);

            // Generate service token
            String serviceToken = jwtUtil.generateServiceToken(user.getId());

            WalletDTO walletDTO = engineWalletClient.getWalletByUserId("Bearer " + serviceToken).getBody();

            return AppResponse.<WalletDTO>builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage("Wallet retrieved successfully")
                    .data(walletDTO)
                    .build();
        } catch (FeignException e) {
            log.error("Error fetching wallet for user {}: {}", email, e.getMessage());
            if (e.status() == 404) {
                throw new RuntimeException("Wallet not found for user: " + email);
            }
            throw new RuntimeException("Failed to fetch wallet: " + e.getMessage());
        }
    }

    @Override
    public AppResponse<TransactionResponse> addMoney(AddMoneyRequest addMoneyRequest) {
        try {
            User user = userRepository.findByEmail(addMoneyRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            log.info("Adding money to wallet for user: {}, amount: {}", addMoneyRequest.getUserId(), addMoneyRequest.getAmount());

            // Generate service token
            String serviceToken = jwtUtil.generateServiceToken(user.getId());

            TransactionResponse transactionResponse = engineWalletClient.addMoney("Bearer " + serviceToken, addMoneyRequest).getBody();

            return AppResponse.<TransactionResponse>builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage(addMoneyRequest.getAmount() + " successfully added to the wallet")
                    .data(transactionResponse)
                    .build();
        } catch (FeignException e) {
            log.error("Error adding money for user {}: {}", addMoneyRequest.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to add money: " + e.getMessage());
        }
    }

    @Override
    public AppResponse<TransactionResponse> withdrawMoney(WithdrawMoneyRequest withdrawMoneyRequest) {
        try {
            User user = userRepository.findByEmail(withdrawMoneyRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            log.info("Withdrawing money from wallet for user: {}, amount: {}", withdrawMoneyRequest.getUserId(), withdrawMoneyRequest.getAmount());

            // Generate service token
            String serviceToken = jwtUtil.generateServiceToken(user.getId());

            TransactionResponse transactionResponse = engineWalletClient.withdrawMoney("Bearer " + serviceToken, withdrawMoneyRequest).getBody();

            return AppResponse.<TransactionResponse>builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage(withdrawMoneyRequest.getAmount() + "successfully retrieved")
                    .data(transactionResponse)
                    .build();
        } catch (FeignException e) {
            log.error("Error withdrawing money for user {}: {}", withdrawMoneyRequest.getUserId(), e.getMessage());
            if (e.status() == 400) {
                throw new RuntimeException("Insufficient balance or invalid request");
            }
            throw new RuntimeException("Failed to withdraw money: " + e.getMessage());
        }
    }

    @Override
    public AppResponse<TransactionHistoryDTO> getTransactionHistory(String email, int page, int size) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            log.info("Fetching transaction history for user: {}, page: {}, size: {}", email, page, size);

            // Generate service token
            String serviceToken = jwtUtil.generateServiceToken(user.getId());

            TransactionHistoryDTO transactionHistoryDTO = engineWalletClient.getTransactionHistory("Bearer " + serviceToken, page, size).getBody();

            return AppResponse.<TransactionHistoryDTO>builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage("Transaction History retrieved successfully")
                    .data(transactionHistoryDTO)
                    .build();
        } catch (FeignException e) {
            log.error("Error fetching transaction history for user {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to fetch transaction history: " + e.getMessage());
        }
    }
}