package com.locally.locally_backend_engine.controller.webhook;

import com.locally.locally_backend_engine.service.StripeWebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

        try {
            log.info("Received Stripe webhook - signature present: {}", sigHeader != null);
            webhookService.handleWebhook(payload, sigHeader);
            log.info("Stripe webhook processed successfully");

            return ResponseEntity.ok("OK");
        } catch (RuntimeException e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage());

            if (e.getMessage() != null && e.getMessage().contains("signature")) {
                return ResponseEntity.badRequest().body("Invalid signature");
            }
            return ResponseEntity.status(500).body("Processing failed");
        } catch (Exception e) {
            log.error("Unexpected error processing Stripe webhook", e);
            return ResponseEntity.status(500).body("Processing failed");
        }
    }
}