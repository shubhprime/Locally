package com.locally.locally_backend_engine.controller.wallet;

import com.locally.locally_backend_engine.dto.TransactionHistoryDTO;
import com.locally.locally_backend_engine.dto.TransactionResponse;
import com.locally.locally_backend_engine.dto.WalletDTO;
import com.locally.locally_backend_engine.dto.WithdrawMoneyRequest;
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
@RequestMapping("/api/engine/v1/delivery-partner/wallet")
public class DeliveryPartnerWalletController {

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

        Long deliveryPartnerId = jwtUtil.retrieveUserIdFromServiceToken(token);

        WalletDTO wallet = walletService.createWallet(deliveryPartnerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @GetMapping("/get-wallet")
    public ResponseEntity<WalletDTO> getWalletByUserId(@RequestHeader("Authorization") String serviceHeader) {
        String token = serviceHeader.substring(7);
        if (!jwtUtil.isServiceTokenValid(token)) {
            throw new RuntimeException("Invalid or expired service token");
        }

        Long deliveryPartnerId = jwtUtil.retrieveUserIdFromServiceToken(token);

        WalletDTO wallet = walletService.getWalletByUserId(deliveryPartnerId);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/withdraw-money")
    public ResponseEntity<TransactionResponse> withdrawMoney(@RequestHeader("Authorization") String serviceHeader, @Valid @RequestBody WithdrawMoneyRequest withdrawMoneyRequest) {
        String token = serviceHeader.substring(7);
        if (!jwtUtil.isServiceTokenValid(token)) {
            throw new RuntimeException("Invalid or expired service token");
        }

        Long deliveryPartnerId = jwtUtil.retrieveUserIdFromServiceToken(token);

        withdrawMoneyRequest.setUserId(deliveryPartnerId);

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

        Long deliveryPartnerId = jwtUtil.retrieveUserIdFromServiceToken(token);

        Pageable pageable = PageRequest.of(page, size);
        TransactionHistoryDTO history = walletService.getTransactionHistory(deliveryPartnerId, pageable);
        return ResponseEntity.ok(history);
    }
}