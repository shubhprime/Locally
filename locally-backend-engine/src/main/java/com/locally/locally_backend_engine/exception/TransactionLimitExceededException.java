package com.locally.locally_backend_engine.exception;

public class TransactionLimitExceededException extends RuntimeException {
    public TransactionLimitExceededException(String message) {
        super(message);
    }
}