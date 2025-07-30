package com.locally.locally_backend_engine.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    private Long userId;

    @NotNull(message = "Email is required")
    private String email; // Set by controller

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "10000.00", message = "Amount must be less than 10000")
    private BigDecimal amount;

    @Pattern(regexp = "^[0-9]{9}$", message = "Invalid routing number")
    private String routingNumber;

    @Pattern(regexp = "^[0-9]{8,17}$", message = "Invalid account number")
    private String bankAccount;

    private String withdrawalMethod;

    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_.,]{0,255}$", message = "Invalid description format")
    private String description;
}