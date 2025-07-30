package com.locally.locally_backend_engine.controller.webhook;

import com.locally.locally_backend_engine.service.StripeWebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/engine/v1/webhook")
public class WebhookController {

    @Autowired
    private StripeWebhookService webhookService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        // Verify signature FIRST
        if (!webhookService.verifySignature(payload, sigHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            log.info("Received Stripe webhook");
            webhookService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
        }
    }
}