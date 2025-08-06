package com.locally.locally_delivery_partner.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawMoneyRequest {
    private Long deliveryPartnerId;
    private String email;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Bank account number is required")
    private String bankAccount;

    private String withdrawalMethod;
    private String description;
}