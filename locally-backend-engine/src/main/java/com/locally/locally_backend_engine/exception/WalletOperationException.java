package com.locally.locally_backend_engine.exception;

public class WalletOperationException extends RuntimeException{
    private final String userSafeMessage;

    public WalletOperationException(String userSafeMessage, String internalMessage, Throwable cause) {
        super(internalMessage, cause);
        this.userSafeMessage = userSafeMessage;
    }

    public WalletOperationException(String userSafeMessage, String internalMessage) {
        super(internalMessage);
        this.userSafeMessage = userSafeMessage;
    }

    public String getUserSafeMessage() {
        return userSafeMessage;
    }
}