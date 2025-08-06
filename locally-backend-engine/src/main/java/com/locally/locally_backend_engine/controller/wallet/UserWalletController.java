package com.locally.locally_backend_engine.controller.wallet;

import com.locally.locally_backend_engine.dto.*;
import com.locally.locally_backend_engine.service.WalletService;
import com.locally.locally_backend_engine.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/engine/v1/user/wallet")
public class UserWalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<WalletDTO> createWallet(@RequestHeader("Authorization") String serviceHeader) {
        String token = serviceHeader.substring(7);
        if (!jwtUtil.isServiceTokenValid(token)) {
            throw new RuntimeException("Invalid or expired service token");
        }

        Long userId = jwtUtil.retrieveUserIdFromServiceToken(token);

        WalletDTO wallet = walletService.createWallet(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @GetMapping("/get-wallet")
    public ResponseEntity<WalletDTO> getWalletByUserId(@RequestHeader("Authorization") String serviceHeader) {
        String token = serviceHeader.substring(7);
        if (!jwtUtil.isServiceTokenValid(token)) {
            throw new RuntimeException("Invalid or expired service token");
        }

        Long userId = jwtUtil.retrieveUserIdFromServiceToken(token);

        WalletDTO wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/add-money")
    public ResponseEntity<TransactionResponse> addMoney(@RequestHeader("Authorization") String serviceHeader, @Valid @RequestBody AddMoneyRequest addMoneyRequest) {
        String token = serviceHeader.substring(7);
        if (!jwtUtil.isServiceTokenValid(token)) {
            throw new RuntimeException("Invalid or expired service token");
        }

        Long userId = jwtUtil.retrieveUserIdFromServiceToken(token);

        addMoneyRequest.setUserId(userId);

        TransactionResponse response = walletService.addMoney(addMoneyRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw-money")
    public ResponseEntity<TransactionResponse> withdrawMoney(@RequestHeader("Authorization") String serviceHeader, @Valid @RequestBody WithdrawMoneyRequest withdrawMoneyRequest) {
        String token = serviceHeader.substring(7);
        if (!jwtUtil.isServiceTokenValid(token)) {
            throw new RuntimeException("Invalid or expired service token");
        }

        Long userId = jwtUtil.retrieveUserIdFromServiceToken(token);

        withdrawMoneyRequest.setUserId(userId);

        TransactionResponse transactionResponse = walletService.withdrawMoney(withdrawMoneyRequest);
        return ResponseEntity.ok(transactionResponse);
    }

    @GetMapping("/transaction/history")
    public ResponseEntity<TransactionHistoryDTO> getTransactionHistory(
            @RequestHeader("Authorization") String serviceHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String token = serviceHeader.substring(7);
        if (!jwtUtil.isServiceTokenValid(token)) {
            throw new RuntimeException("Invalid or expired service token");
        }

        Long userId = jwtUtil.retrieveUserIdFromServiceToken(token);

        Pageable pageable = PageRequest.of(page, size);
        TransactionHistoryDTO history = walletService.getTransactionHistory(userId, pageable);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        // This will be handled by a separate webhook service
        // For now, just acknowledge receipt
        return ResponseEntity.ok("Webhook received");
    }
}