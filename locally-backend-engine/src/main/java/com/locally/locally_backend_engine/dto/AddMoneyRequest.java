package com.locally.locally_backend_engine.dto;

import com.locally.locally_backend_engine.model.PaymentMethod;
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
public class AddMoneyRequest {
    private Long userId;

    @NotNull(message = "Email is required")
    private String email; // Set by controller

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @DecimalMax(value = "10000.00", message = "Amount must be less than 10000")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod; // "CARD", "BANK_TRANSFER", "PAYPAL"

    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_.,]{0,255}$", message = "Invalid description format")
    private String description;
}