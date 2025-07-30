package com.locally.backend.controller.address;

import com.locally.backend.dto.AddressListResponse;
import com.locally.backend.dto.AddressRequest;
import com.locally.backend.dto.AppResponse;
import com.locally.backend.service.AddressService;
import com.locally.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/v1/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<AppResponse> createAddress(@RequestBody AddressRequest addressRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse response = addressService.createAddress(email, addressRequest);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<AddressListResponse> getUserAddresses(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AddressListResponse response = addressService.getUserAddresses(email);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AppResponse> getAddressById(@PathVariable Long addressId, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse response = addressService.getAddressById(email, addressId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AppResponse> updateAddress(@PathVariable Long addressId, @RequestBody AddressRequest addressRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse response = addressService.updateAddress(email, addressId, addressRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<AppResponse> deleteAddress(@PathVariable Long addressId, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse response = addressService.deleteAddress(email, addressId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PatchMapping("/{addressId}/set-default")
    public ResponseEntity<AppResponse> setDefaultAddress(@PathVariable Long addressId, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse response = addressService.setDefaultAddress(email, addressId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}