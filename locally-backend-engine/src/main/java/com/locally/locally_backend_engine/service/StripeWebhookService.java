package com.locally.locally_backend_engine.service;

public interface StripeWebhookService {
    void init();

    void handleWebhook(String payload, String sigHeader);

    public boolean verifySignature(String payload, String sigHeader);
}