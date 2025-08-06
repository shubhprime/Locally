package com.locally.locally_delivery_partner.model;

public enum TransactionStatus {

    PENDING("PENDING", "Transaction is pending"),
    PROCESSING("PROCESSING", "Transaction is being processed"),
    COMPLETED("COMPLETED", "Transaction completed successfully"),
    FAILED("FAILED", "Transaction failed"),
    CANCELLED("CANCELLED", "Transaction was cancelled"),
    REFUNDED("REFUNDED", "Transaction was refunded");

    private final String code;
    private final String description;

    TransactionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}