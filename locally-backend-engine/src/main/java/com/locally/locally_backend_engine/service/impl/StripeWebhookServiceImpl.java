package com.locally.locally_backend_engine.service.impl;

import com.locally.locally_backend_engine.model.Transaction;
import com.locally.locally_backend_engine.model.TransactionStatus;
import com.locally.locally_backend_engine.repository.TransactionRepository;
import com.locally.locally_backend_engine.service.StripeWebhookService;
import com.locally.locally_backend_engine.service.WalletService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class StripeWebhookServiceImpl implements StripeWebhookService {

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    // Lock per payment intent to prevent concurrent processing
    private final ConcurrentHashMap<String, ReentrantLock> paymentIntentLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lockCreationTime = new ConcurrentHashMap<>();
    private final Set<String> processedEvents = ConcurrentHashMap.newKeySet();

    private final ConcurrentHashMap<String, LocalDateTime> processedEventIds = new ConcurrentHashMap<>();

    private static final long LOCK_CLEANUP_THRESHOLD_MS = 300000;
    private static final long EVENT_DEDUP_THRESHOLD_HOURS = 24;

    @Override
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Initialized Stripe webhook service with webhook secret configured");
    }

    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void handleWebhook(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
            throw new RuntimeException("Invalid webhook signature");
        }

        log.info("Received Stripe webhook event: {} with ID: {}", event.getType(), event.getId());

        if (isDuplicateEvent(event)) {
            log.info("Duplicate webhook event detected, skipping: {}", event.getId());
            return;
        }

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "payment_intent.canceled":
                    handlePaymentIntentCanceled(event);
                    break;
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }

            markEventAsProcessed(event);

        } catch (Exception e) {
            log.error("Error processing webhook event {}: {}", event.getId(), e.getMessage(), e);
            throw e;
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = extractPaymentIntent(event);
        if (paymentIntent == null) return;

        String paymentIntentId = paymentIntent.getId();
        log.info("Payment succeeded for PaymentIntent: {}", paymentIntentId);

        processWithLock(paymentIntentId, () -> {
            walletService.processSuccessfulPayment(paymentIntentId);
        });
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = extractPaymentIntent(event);
        if (paymentIntent == null) return;

        String paymentIntentId = paymentIntent.getId();
        log.info("Payment failed for PaymentIntent: {}", paymentIntentId);

        String failureReason = paymentIntent.getLastPaymentError() != null ?
                paymentIntent.getLastPaymentError().getMessage() : "Payment failed";

        processWithLock(paymentIntentId, () -> {
            updateTransactionStatus(paymentIntentId, TransactionStatus.FAILED, failureReason);
        });
    }

    private void handlePaymentIntentCanceled(Event event) {
        PaymentIntent paymentIntent = extractPaymentIntent(event);
        if (paymentIntent == null) return;

        String paymentIntentId = paymentIntent.getId();
        log.info("Payment canceled for PaymentIntent: {}", paymentIntentId);

        processWithLock(paymentIntentId, () -> {
            updateTransactionStatus(paymentIntentId, TransactionStatus.CANCELLED, "Payment was canceled");
        });
    }

    private void handlePaymentIntentRequiresAction(Event event) {
        PaymentIntent paymentIntent = extractPaymentIntent(event);
        if (paymentIntent == null) return;

        String paymentIntentId = paymentIntent.getId();
        log.info("Payment requires action for PaymentIntent: {}", paymentIntentId);

        processWithLock(paymentIntentId, () -> {
            updateTransactionStatus(paymentIntentId, TransactionStatus.PENDING, "Payment requires additional action");
        });
    }

    private void handlePaymentIntentProcessing(Event event) {
        PaymentIntent paymentIntent = extractPaymentIntent(event);
        if (paymentIntent == null) return;

        String paymentIntentId = paymentIntent.getId();
        log.info("Payment processing for PaymentIntent: {}", paymentIntentId);

        processWithLock(paymentIntentId, () -> {
            updateTransactionStatus(paymentIntentId, TransactionStatus.PENDING, "Payment is being processed");
        });
    }

    private void processWithLock(String paymentIntentId, Runnable processor) {
        ReentrantLock lock = paymentIntentLocks.computeIfAbsent(paymentIntentId, k -> {
            lockCreationTime.put(k, System.currentTimeMillis());
            return new ReentrantLock();
        });

        try {
            lock.lock();
            processor.run();
        } catch (Exception e) {
            log.error("Error processing webhook for PaymentIntent {}: {}", paymentIntentId, e.getMessage(), e);
            throw e;
        } finally {
            lock.unlock();

            // Clean up lock immediately if no other threads are waiting
            if (!lock.hasQueuedThreads()) {
                paymentIntentLocks.remove(paymentIntentId);
                lockCreationTime.remove(paymentIntentId);
            }
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    private void updateTransactionStatus(String paymentIntentId, TransactionStatus status, String failureReason) {
        Optional<Transaction> transactionOpt = transactionRepository.findByStripePaymentIntentId(paymentIntentId);

        if (transactionOpt.isEmpty()) {
            log.warn("Transaction not found for PaymentIntent: {}", paymentIntentId);
            return;
        }

        Transaction transaction = transactionOpt.get();

        // Check if transaction is already in a final state
        if (transaction.isFinalStatus() && status != TransactionStatus.COMPLETED) {
            log.info("Transaction {} is already in final state: {}, skipping update to: {}",
                    transaction.getTransactionId(), transaction.getStatus(), status);
            return;
        }
        // Validate state transitions
        if (!isValidStatusTransition(transaction.getStatus(), status)) {
            log.warn("Invalid status transition for transaction {}: {} -> {}",
                    transaction.getTransactionId(), transaction.getStatus(), status);
            return;
        }

        // Update transaction status
        TransactionStatus previousStatus = transaction.getStatus();
        transaction.setStatus(status);
        transaction.setFailureReason(failureReason);
        transaction.setWebhookProcessed(true);

        if (status.equals(TransactionStatus.FAILED) || status.equals(TransactionStatus.CANCELLED)) {
            transaction.markAsProcessed();
        }

        transactionRepository.save(transaction);

        log.info("Updated transaction {} status from {} to {} for PaymentIntent {}",
                transaction.getTransactionId(), previousStatus, status, paymentIntentId);
    }

    private boolean isValidStatusTransition(TransactionStatus from, TransactionStatus to) {
        // Define valid transitions
        switch (from) {
            case PENDING:
                return to == TransactionStatus.COMPLETED ||
                        to == TransactionStatus.FAILED ||
                        to == TransactionStatus.CANCELLED;
            case PROCESSING:
                return to == TransactionStatus.COMPLETED ||
                        to == TransactionStatus.FAILED;
            case COMPLETED:
                return false; // No transitions from completed
            case FAILED:
                return false; // No transitions from failed
            case CANCELLED:
                return false; // No transitions from cancelled
            default:
                return true; // Allow unknown transitions for safety
        }
    }

    private boolean isDuplicateEvent(Event event) {
        String eventId = event.getId();
        LocalDateTime now = LocalDateTime.now();

        if (processedEventIds.containsKey(eventId)) {
            return true;
        }

        processedEventIds.put(eventId, now);
        return false;
    }

    private void markEventAsProcessed(Event event) {
        processedEvents.add(event.getId());
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        try {
            return (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        } catch (Exception e) {
            log.error("Failed to extract PaymentIntent from webhook event {}: {}", event.getId(), e.getMessage());
            return null;
        }
    }

    public boolean verifySignature(String payload, String sigHeader) {
        try {
            // Verify the Stripe signature
            com.stripe.model.Event event = com.stripe.net.Webhook.constructEvent(
                    payload,
                    sigHeader,
                    webhookSecret
            );
            return event != null;
        } catch (com.stripe.exception.SignatureVerificationException e) {
            return false; // Invalid signature
        }
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupResources() {
        long now = System.currentTimeMillis();
        int locksRemoved = 0;
        int eventsRemoved = 0;

        // Clean up old locks
        lockCreationTime.entrySet().removeIf(entry -> {
            String paymentIntentId = entry.getKey();
            long creationTime = entry.getValue();

            if (now - creationTime > LOCK_CLEANUP_THRESHOLD_MS) {
                ReentrantLock lock = paymentIntentLocks.get(paymentIntentId);
                if (lock != null && !lock.isLocked() && !lock.hasQueuedThreads()) {
                    paymentIntentLocks.remove(paymentIntentId);
                    return true;
                }
            }
            return false;
        });

        locksRemoved = lockCreationTime.size();

        // Clean up old processed event IDs (prevent memory growth)
        LocalDateTime cutoff = LocalDateTime.now().minusHours(EVENT_DEDUP_THRESHOLD_HOURS);
        processedEventIds.entrySet().removeIf(entry -> {
            return entry.getValue().isBefore(cutoff);
        });

        eventsRemoved = processedEventIds.size();

        // Clean up processed events set
        if (processedEvents.size() > 10000) { // Prevent unbounded growth
            processedEvents.clear();
        }

        if (locksRemoved > 0 || eventsRemoved > 0) {
            log.debug("Cleaned up webhook resources - locks: {}, eventIds: {}, processedEvents: {}",
                    paymentIntentLocks.size(), processedEventIds.size(), processedEvents.size());
        }
    }
}