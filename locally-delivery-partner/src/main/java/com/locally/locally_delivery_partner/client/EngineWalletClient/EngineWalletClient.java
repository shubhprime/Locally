package com.locally.locally_delivery_partner.client.EngineWalletClient;

import com.locally.locally_delivery_partner.dto.TransactionHistoryDTO;
import com.locally.locally_delivery_partner.dto.TransactionResponse;
import com.locally.locally_delivery_partner.dto.WalletDTO;
import com.locally.locally_delivery_partner.dto.WithdrawMoneyRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "engine-delivery-partner-wallet-service", url = "${engine.service.url}")
public interface EngineWalletClient {

    @PostMapping("/api/engine/v1/delivery-partner/wallet/create")
    ResponseEntity<WalletDTO> createWallet(@RequestHeader("Authorization") String serviceToken);

    @GetMapping("/api/engine/v1/delivery-partner/wallet/get-wallet")
    ResponseEntity<WalletDTO> getWalletByUserId(@RequestHeader("Authorization") String serviceToken);

    @PostMapping("/api/engine/v1/delivery-partner/wallet/withdraw-money")
    ResponseEntity<TransactionResponse> withdrawMoney(@RequestHeader("Authorization") String serviceToken, @RequestBody WithdrawMoneyRequest withdrawMoneyRequest);

    @GetMapping("/api/engine/v1/delivery-partner/wallet/transaction/history")
    ResponseEntity<TransactionHistoryDTO> getTransactionHistory(
            @RequestHeader("Authorization") String serviceToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);
}