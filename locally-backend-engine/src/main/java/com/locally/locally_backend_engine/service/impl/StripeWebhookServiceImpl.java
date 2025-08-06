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
import java.util.concurrent.atomic.AtomicInteger;
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

    // Removed redundant processedEvents Set - using only processedEventIds
    private final ConcurrentHashMap<String, LocalDateTime> processedEventIds = new ConcurrentHashMap<>();

    private static final long LOCK_CLEANUP_THRESHOLD_MS = 300000; // 5 minutes
    private static final long EVENT_DEDUP_THRESHOLD_HOURS = 24;
    private static final int MAX_PROCESSED_EVENTS = 10000;

    @Override
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Initialized Stripe webhook service with webhook secret configured");
    }

    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void handleWebhook(String payload, String sigHeader) {

        // Input validation
        if (payload == null || payload.trim().isEmpty()) {
            throw new IllegalArgumentException("Webhook payload cannot be null or empty");
        }
        if (sigHeader == null || sigHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Stripe signature header cannot be null or empty");
        }

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
                case "payment_intent.requires_action":
                    handlePaymentIntentRequiresAction(event);
                    break;
                case "payment_intent.processing":
                    handlePaymentIntentProcessing(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "payment_intent.canceled":
                    handlePaymentIntentCanceled(event);
                    break;
                case "payment_intent.created":
                    handlePaymentIntentCreated(event);
                    break;
                case "charge.succeeded":
                    handleChargeSucceeded(event);
                    break;
                case "charge.failed":
                    handleChargeFailed(event);
                    break;
                case "charge.dispute.created":
                    handleChargeDisputeCreated(event);
                    break;
                case "payment_method.attached":
                    handlePaymentMethodAttached(event);
                    break;
                case "invoice.payment_succeeded":
                    handleInvoicePaymentSucceeded(event);
                    break;
                case "customer.created":
                    handleCustomerCreated(event);
                    break;
                case "transfer.reversed":
                    handleTransferReversed(event);
                    break;
                case "transfer.updated":
                    handleTransferUpdated(event);
                    break;
                case "payout.failed":
                    handlePayoutFailed(event);
                    break;
                case "refund.updated":
                    handleRefundUpdated(event);
                    break;
                case "account.updated":
                    handleAccountUpdated(event);
                    break;

                // WALLET-SPECIFIC EVENTS (for drivers)
                case "transfer.created":
                    handleTransferCreated(event);
                    break;
                case "transfer.paid":
                    handleTransferPaid(event);
                    break;
                case "transfer.failed":
                    handleTransferFailed(event);
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

    private void handlePaymentIntentCreated(Event event) {
        PaymentIntent paymentIntent = extractPaymentIntent(event);
        if (paymentIntent == null) return;

        String paymentIntentId = paymentIntent.getId();
        log.info("Payment intent created for PaymentIntent: {} - Driver payment initiated", paymentIntentId);

        processWithLock(paymentIntentId, () -> {
            updateTransactionStatus(paymentIntentId, TransactionStatus.PENDING, "Payment to delivery partner initiated");
        });
    }

    private void handleChargeSucceeded(Event event) {
        // Extract charge information for detailed tracking
        log.info("Charge succeeded - money movement confirmed");
        // Additional logging/tracking if needed
    }

    private void handleChargeFailed(Event event) {
        // Handle charge-level failures
        log.warn("Charge failed - investigating payment failure");
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
            updateTransactionStatus(paymentIntentId, TransactionStatus.PROCESSING, "Payment is being processed");
        });
    }

    private void handleTransferCreated(Event event) {
        log.info("Transfer to driver account created");
    }

    private void handleTransferPaid(Event event) {
        log.info("Transfer to driver account completed");
    }

    private void handleTransferFailed(Event event) {
        log.warn("Transfer to driver account failed - may need manual intervention");
    }

    private void handleChargeDisputeCreated(Event event) {
        log.warn("Charge dispute created - manual review may be required");
    }

    private void handlePaymentMethodAttached(Event event) {
        log.info("Payment method successfully attached to a customer");
    }

    private void handleInvoicePaymentSucceeded(Event event) {
        log.info("Invoice payment succeeded - subscription or recurring billing completed");
    }

    private void handleCustomerCreated(Event event) {
        log.info("New customer created in Stripe");
    }

    private void handleTransferReversed(Event event) {
        log.warn("Transfer reversed - may require driver wallet adjustment");
    }

    private void handleTransferUpdated(Event event) {
        log.info("Transfer updated - new status recorded");
    }

    private void handlePayoutFailed(Event event) {
        log.error("Payout failed - review Stripe dashboard for details");
    }

    private void handleRefundUpdated(Event event) {
        log.info("Refund updated - check for changes in refund status");
    }

    private void handleAccountUpdated(Event event) {
        log.info("Stripe account updated - verify changes if needed");
    }

    private void processWithLock(String paymentIntentId, Runnable processor) {
        ReentrantLock lock = paymentIntentLocks.computeIfAbsent(paymentIntentId, k -> {
            lockCreationTime.put(k, System.currentTimeMillis());
            return new ReentrantLock();
        });

        boolean lockAcquired = false;
        try {
            lockAcquired = lock.tryLock(5, java.util.concurrent.TimeUnit.SECONDS);
            if (!lockAcquired) {
                log.warn("Failed to acquire lock for PaymentIntent {} within timeout", paymentIntentId);
                throw new RuntimeException("Lock acquisition timeout");
            }

            processor.run();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while acquiring lock", e);
        } catch (Exception e) {
            log.error("Error processing webhook for PaymentIntent {}: {}", paymentIntentId, e.getMessage(), e);
            throw e;
        } finally {
            if (lockAcquired) {
                lock.unlock();
            }

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
                        to == TransactionStatus.CANCELLED ||
                        to == TransactionStatus.PROCESSING;
            case PROCESSING:
                return to == TransactionStatus.COMPLETED ||
                        to == TransactionStatus.FAILED ||
                        to == TransactionStatus.CANCELLED;
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

        return processedEventIds.putIfAbsent(eventId, now) != null;
    }

    private void markEventAsProcessed(Event event) {
        log.debug("Event {} marked as processed", event.getId());
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
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            return event != null;
        } catch (SignatureVerificationException e) {
            log.debug("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupResources() {
        long now = System.currentTimeMillis();
        AtomicInteger locksRemoved = new AtomicInteger(0);
        AtomicInteger eventsRemoved = new AtomicInteger(0);

        // Clean up old locks
        lockCreationTime.entrySet().removeIf(entry -> {
            String paymentIntentId = entry.getKey();
            long creationTime = entry.getValue();

            if (now - creationTime > LOCK_CLEANUP_THRESHOLD_MS) {
                ReentrantLock lock = paymentIntentLocks.get(paymentIntentId);
                if (lock != null && !lock.isLocked() && !lock.hasQueuedThreads()) {
                    paymentIntentLocks.remove(paymentIntentId);
                    locksRemoved.incrementAndGet();
                    return true;
                }
            }
            return false;
        });

        // Clean up old processed event IDs (prevent memory growth)
        LocalDateTime cutoff = LocalDateTime.now().minusHours(EVENT_DEDUP_THRESHOLD_HOURS);
        processedEventIds.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(cutoff)) {
                eventsRemoved.incrementAndGet();
                return true;
            }
            return false;
        });

        // Clean up processed events set
        if (processedEventIds.size() > MAX_PROCESSED_EVENTS) { // Prevent unbounded growth
            int toRemove = processedEventIds.size() - (MAX_PROCESSED_EVENTS / 2);
            processedEventIds.entrySet().stream()
                    .sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                    .limit(toRemove)
                    .forEach(entry -> {
                        processedEventIds.remove(entry.getKey());
                        eventsRemoved.incrementAndGet();
                    });
        }

        if (locksRemoved.get() > 0 || eventsRemoved.get() > 0) {
            log.info("Cleaned up webhook resources - locks removed: {}, events removed: {}, " +
                            "active locks: {}, tracked events: {}",
                    locksRemoved.get(), eventsRemoved.get(),
                    paymentIntentLocks.size(), processedEventIds.size());
        }
    }
}