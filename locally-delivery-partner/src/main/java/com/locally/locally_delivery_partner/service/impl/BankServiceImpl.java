package com.locally.locally_delivery_partner.service.impl;

import com.locally.locally_delivery_partner.dto.AppResponse;
import com.locally.locally_delivery_partner.dto.BankDetailsRequest;
import com.locally.locally_delivery_partner.dto.BankDetailsResponse;
import com.locally.locally_delivery_partner.model.DeliveryPartner;
import com.locally.locally_delivery_partner.repository.DeliveryPartnerRepository;
import com.locally.locally_delivery_partner.service.BankService;
import com.locally.locally_delivery_partner.utils.DeliveryPartnerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BankServiceImpl implements BankService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Override
    public AppResponse<Void> addOrUpdateBankDetails(BankDetailsRequest bankDetailsRequest) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(bankDetailsRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

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

        deliveryPartner.setAccountHolderName(bankDetailsRequest.getAccountHolderName());
        deliveryPartner.setBankName(bankDetailsRequest.getBankName());
        deliveryPartner.setRoutingNumber(bankDetailsRequest.getRoutingNumber());
        deliveryPartner.setBankAccountNumber(bankDetailsRequest.getBankAccountNumber());
        deliveryPartner.setAccountType(bankDetailsRequest.getAccountType());

        deliveryPartnerRepository.save(deliveryPartner);

        return AppResponse.<Void>builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Bank details updated successfully.")
                .data(null)
                .build();
    }

    @Override
    public AppResponse<BankDetailsResponse> getBankDetails(String email) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        boolean hasBankDetails = (deliveryPartner.getAccountHolderName() != null && !deliveryPartner.getAccountHolderName().trim().isEmpty()) ||
                (deliveryPartner.getBankName() != null && !deliveryPartner.getBankName().trim().isEmpty()) ||
                (deliveryPartner.getRoutingNumber() != null && !deliveryPartner.getRoutingNumber().trim().isEmpty()) ||
                (deliveryPartner.getBankAccountNumber() != null && !deliveryPartner.getBankAccountNumber().trim().isEmpty()) ||
                (deliveryPartner.getAccountType() != null && !deliveryPartner.getAccountType().trim().isEmpty());

        BankDetailsResponse bankDetailsResponse = BankDetailsResponse.builder()
                .accountHolderName(deliveryPartner.getAccountHolderName())
                .bankName(deliveryPartner.getBankName())
                .routingNumber(deliveryPartner.getRoutingNumber())
                .bankAccountNumber(deliveryPartner.getBankAccountNumber())
                .accountType(deliveryPartner.getAccountType())
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
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        deliveryPartner.setAccountHolderName(null);
        deliveryPartner.setBankName(null);
        deliveryPartner.setRoutingNumber(null);
        deliveryPartner.setBankAccountNumber(null);
        deliveryPartner.setAccountType(null);

        deliveryPartnerRepository.save(deliveryPartner);

        return AppResponse.<Void>builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Bank details deleted successfully.")
                .data(null)
                .build();
    }
}