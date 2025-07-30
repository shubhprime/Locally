package com.locally.locally_backend_engine.model;

public enum TransactionType {

    ADD_MONEY("ADD_MONEY", "Money added to wallet"),
    WITHDRAW("WITHDRAW", "Money withdrawn from wallet"),
    PAYMENT("PAYMENT", "Payment made using wallet"),
    REFUND("REFUND", "Refund received");

    private final String code;
    private final String description;

    TransactionType(String code, String description) {
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