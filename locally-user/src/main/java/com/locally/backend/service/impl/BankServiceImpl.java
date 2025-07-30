package com.locally.backend.service.impl;

import com.locally.backend.dto.AppResponse;
import com.locally.backend.dto.BankDetailsRequest;
import com.locally.backend.dto.BankDetailsResponse;
import com.locally.backend.model.User;
import com.locally.backend.repository.UserRepository;
import com.locally.backend.service.BankService;
import com.locally.backend.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BankServiceImpl implements BankService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public AppResponse<Void> addOrUpdateBankDetails(BankDetailsRequest bankDetailsRequest) {
        User user = userRepository.findByEmailAndIsDeletedFalse(bankDetailsRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        if (bankDetailsRequest.getAccountHolderName() == null ||
                bankDetailsRequest.getBankName() == null ||
                bankDetailsRequest.getRoutingNumber() == null ||
                bankDetailsRequest.getBankAccountNumber() == null ||
                bankDetailsRequest.getAccountType() == null) {

            return AppResponse.<Void>builder()
                    .responseCode("400")
                    .success(false)
                    .responseMessage("All bank details fields must be provided.")
                    .data(null)
                    .build();
        }

        user.setAccountHolderName(bankDetailsRequest.getAccountHolderName());
        user.setBankName(bankDetailsRequest.getBankName());
        user.setRoutingNumber(bankDetailsRequest.getRoutingNumber());
        user.setBankAccountNumber(bankDetailsRequest.getBankAccountNumber());
        user.setAccountType(bankDetailsRequest.getAccountType());

        userRepository.save(user);

        return AppResponse.<Void>builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Bank details updated successfully.")
                .data(null)
                .build();
    }

    @Override
    public AppResponse<BankDetailsResponse> getBankDetails(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        boolean hasBankDetails = (user.getAccountHolderName() != null && !user.getAccountHolderName().trim().isEmpty()) ||
                (user.getBankName() != null && !user.getBankName().trim().isEmpty()) ||
                (user.getRoutingNumber() != null && !user.getRoutingNumber().trim().isEmpty()) ||
                (user.getBankAccountNumber() != null && !user.getBankAccountNumber().trim().isEmpty()) ||
                (user.getAccountType() != null && !user.getAccountType().trim().isEmpty());

        BankDetailsResponse bankDetailsResponse = BankDetailsResponse.builder()
                .accountHolderName(user.getAccountHolderName())
                .bankName(user.getBankName())
                .routingNumber(user.getRoutingNumber())
                .bankAccountNumber(user.getBankAccountNumber())
                .accountType(user.getAccountType())
                .hasBankDetails(hasBankDetails)
                .build();

        return AppResponse.<BankDetailsResponse>builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Bank details retrieved successfully.")
                .data(bankDetailsResponse)
                .build();
    }

    @Override
    public AppResponse<Void> deleteBankDetails(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        user.setAccountHolderName(null);
        user.setBankName(null);
        user.setRoutingNumber(null);
        user.setBankAccountNumber(null);
        user.setAccountType(null);

        userRepository.save(user);

        return AppResponse.<Void>builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Bank details deleted successfully.")
                .data(null)
                .build();
    }
}