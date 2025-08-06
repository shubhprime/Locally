package com.locally.locally_delivery_partner.controller.wallet;

import com.locally.locally_delivery_partner.dto.*;
import com.locally.locally_delivery_partner.service.WalletService;
import com.locally.locally_delivery_partner.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/delivery-partner/v1/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<AppResponse<WalletDTO>> createWallet(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse<WalletDTO> wallet = walletService.createWallet(email);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @GetMapping("/get-wallet")
    public ResponseEntity<AppResponse<WalletDTO>> getWalletByUserId(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse<WalletDTO> wallet = walletService.getWalletByUserId(email);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/withdraw-money")
    public ResponseEntity<AppResponse<TransactionResponse>> withdrawMoney(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody WithdrawMoneyRequest withdrawMoneyRequest) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        withdrawMoneyRequest.setEmail(email);

        AppResponse<TransactionResponse> transactionResponse = walletService.withdrawMoney(withdrawMoneyRequest);
        return ResponseEntity.ok(transactionResponse);
    }

    @GetMapping("/transaction/history")
    public ResponseEntity<AppResponse<TransactionHistoryDTO>> getTransactionHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse<TransactionHistoryDTO> transactionHistory = walletService.getTransactionHistory(email, page, size);
        return ResponseEntity.ok(transactionHistory);
    }

    @GetMapping("/balance")
    public ResponseEntity<AppResponse<Object>> getWalletBalance(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse<WalletDTO> wallet = walletService.getWalletByUserId(email);

        Map<String, Object> responseData = Map.of(
                "userId", wallet.getData().getDeliveryPartnerId(),
                "balance", wallet.getData().getBalance(),
                "isActive", wallet.getData().getIsActive()
        );

        AppResponse<Object> response = AppResponse.<Object>builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Wallet balance fetched successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }
}