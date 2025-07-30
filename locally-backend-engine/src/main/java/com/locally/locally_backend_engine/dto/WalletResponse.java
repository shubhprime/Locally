package com.locally.locally_backend_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private Double availableBalance;
    private Double totalAddedMoney; // Total money user has added
    private Double totalWithdrawnMoney; // Total money user has withdrawn
    private boolean hasBankDetails; // Can user withdraw?
    private List<String> availablePaymentMethods;
}