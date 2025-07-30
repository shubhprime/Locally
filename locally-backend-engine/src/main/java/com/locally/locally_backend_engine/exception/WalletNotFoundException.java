package com.locally.locally_backend_engine.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}