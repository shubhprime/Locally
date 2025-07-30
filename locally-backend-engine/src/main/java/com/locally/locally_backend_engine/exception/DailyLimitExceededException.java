package com.locally.locally_backend_engine.exception;

public class DailyLimitExceededException extends RuntimeException {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}