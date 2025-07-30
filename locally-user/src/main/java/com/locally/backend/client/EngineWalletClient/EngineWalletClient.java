package com.locally.backend.client.EngineWalletClient;

import com.locally.backend.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "engine-wallet-service", url = "${engine.service.url}")
public interface EngineWalletClient {

    @PostMapping("/api/engine/v1/user/wallet/create")
    ResponseEntity<WalletDTO> createWallet(@RequestHeader("Authorization") String serviceToken);

    @GetMapping("/api/engine/v1/user/wallet/get-wallet")
    ResponseEntity<WalletDTO> getWalletByUserId(@RequestHeader("Authorization") String serviceToken);

    @PostMapping("/api/engine/v1/user/wallet/add-money")
    ResponseEntity<TransactionResponse> addMoney(@RequestHeader("Authorization") String serviceToken, @RequestBody AddMoneyRequest addMoneyRequest);

    @PostMapping("/api/engine/v1/user/wallet/withdraw-money")
    ResponseEntity<TransactionResponse> withdrawMoney(@RequestHeader("Authorization") String serviceToken, @RequestBody WithdrawMoneyRequest withdrawMoneyRequest);

    @GetMapping("/api/engine/v1/user/wallet/transaction/history")
    ResponseEntity<TransactionHistoryDTO> getTransactionHistory(
            @RequestHeader("Authorization") String serviceToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);
}