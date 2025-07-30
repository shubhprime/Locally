package com.locally.backend.service;

import com.locally.backend.dto.AppResponse;
import com.locally.backend.dto.BankDetailsRequest;
import com.locally.backend.dto.BankDetailsResponse;

public interface BankService {
    AppResponse<Void> addOrUpdateBankDetails(BankDetailsRequest bankDetailsRequest);
    AppResponse<BankDetailsResponse> getBankDetails(String email);
    AppResponse<Void> deleteBankDetails(String email);
}