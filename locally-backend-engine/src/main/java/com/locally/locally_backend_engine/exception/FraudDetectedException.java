package com.locally.locally_backend_engine.exception;

public class FraudDetectedException extends RuntimeException {
    public FraudDetectedException(String message) {
        super(message);
    }
}