package com.locally.locally_backend_engine.model;

public enum PaymentMethod {

    CARD("CARD", "Credit/Debit Card"),
    BANK_TRANSFER("BANK_TRANSFER", "Bank Transfer"),
    UPI("UPI", "UPI Payment"),
    WALLET("WALLET", "Wallet Balance");

    private final String code;
    private final String description;

    PaymentMethod(String code, String description) {
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