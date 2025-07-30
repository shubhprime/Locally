package com.locally.backend.controller.bank;

import com.locally.backend.dto.AppResponse;
import com.locally.backend.dto.BankDetailsRequest;
import com.locally.backend.dto.BankDetailsResponse;
import com.locally.backend.service.BankService;
import com.locally.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/v1/bank")
public class BankController {
    @Autowired
    private BankService bankService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/bank-details")
    public ResponseEntity<AppResponse<Void>> addOrUpdateBankDetails(@RequestBody BankDetailsRequest bankDetailsRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        bankDetailsRequest.setEmail(email);

        AppResponse appResponse = bankService.addOrUpdateBankDetails(bankDetailsRequest);

        if (appResponse.isSuccess()) {
            return ResponseEntity.ok(appResponse);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(appResponse);
        }
    }

    @GetMapping("/bank-details")
    public ResponseEntity<AppResponse<BankDetailsResponse>> getBankDetails(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse appResponse = bankService.getBankDetails(email);

        if (appResponse.isSuccess()) {
            return ResponseEntity.ok(appResponse);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(appResponse);
        }
    }

    @DeleteMapping("/bank-details")
    public ResponseEntity<AppResponse<Void>> deleteBankDetails(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse appResponse = bankService.deleteBankDetails(email);

        if (appResponse.isSuccess()) {
            return ResponseEntity.ok(appResponse);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(appResponse);
        }
    }
}