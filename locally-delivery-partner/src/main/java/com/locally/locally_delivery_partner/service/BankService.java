package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.AppResponse;
import com.locally.locally_delivery_partner.dto.BankDetailsRequest;
import com.locally.locally_delivery_partner.dto.BankDetailsResponse;

public interface BankService {
    AppResponse<Void> addOrUpdateBankDetails(BankDetailsRequest bankDetailsRequest);
    AppResponse<BankDetailsResponse> getBankDetails(String email);
    AppResponse<Void> deleteBankDetails(String email);
}