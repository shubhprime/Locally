package com.locally.locally_delivery_partner.controller.auth;

import com.locally.locally_delivery_partner.dto.*;
import com.locally.locally_delivery_partner.service.DeliveryPartnerService;
import com.locally.locally_delivery_partner.utils.DeliveryPartnerUtils;
import com.locally.locally_delivery_partner.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/delivery-partner/v1/auth")
public class DeliveryPartnerController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DeliveryPartnerService deliveryPartnerService;

    @PostMapping("/signup")
    public ResponseEntity<DeliveryPartnerResponse> createUser(@RequestBody CreateDeliveryPartnerRequest createDeliveryPartnerRequest) {
        DeliveryPartnerResponse deliveryPartnerResponse = deliveryPartnerService.createDeliveryPartner(createDeliveryPartnerRequest);

        if (deliveryPartnerResponse.getResponseCode().equals(DeliveryPartnerUtils.ACCOUNT_EXISTS_CODE)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(deliveryPartnerResponse); // 409 Conflict if account exists
        }

        if (deliveryPartnerResponse.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(deliveryPartnerResponse); // 201 Created
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(deliveryPartnerResponse); // Generic fallback
        }
    }

    @PostMapping("/send-verification-otp")
    public ResponseEntity<DeliveryPartnerResponse> sendVerificationOtp(@RequestBody SendVerificationOtpRequest sendVerificationOtpRequest) {
        DeliveryPartnerResponse response = deliveryPartnerService.sendVerificationOtp(sendVerificationOtpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/verify-verification-otp")
    public ResponseEntity<DeliveryPartnerResponse> verifyVerificationOtp(@RequestBody OtpVerificationRequest request) {
        DeliveryPartnerResponse appResponse = deliveryPartnerService.verifyVerificationOtp(request);
        return ResponseEntity.status(HttpStatus.OK).body(appResponse);
    }

    @PostMapping("/upload-profile-picture")
    public ResponseEntity<DeliveryPartnerResponse> uploadProfilePicture(@RequestParam("picture") MultipartFile picture, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        UploadProfilePictureRequest uploadProfilePictureRequest = new UploadProfilePictureRequest(email, picture);

        DeliveryPartnerResponse deliveryPartnerResponse = deliveryPartnerService.uploadProfilePicture(uploadProfilePictureRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryPartnerResponse);
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<DeliveryPartnerResponse> updateProfile(@RequestBody UpdateDeliveryPartnerRequest updateDeliveryPartnerRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        updateDeliveryPartnerRequest.setEmail(email);

        DeliveryPartnerResponse deliveryPartnerResponse = deliveryPartnerService.updateDeliveryPartnerProfile(updateDeliveryPartnerRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryPartnerResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<DeliveryPartnerResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        DeliveryPartnerResponse deliveryPartnerResponse = deliveryPartnerService.loginDeliveryPartner(loginRequest);

        if (deliveryPartnerResponse.isSuccess()) {
            return ResponseEntity.ok(deliveryPartnerResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(deliveryPartnerResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<DeliveryPartnerResponse> logoutUser(@RequestBody RefreshTokenRequest userRequest) {
        DeliveryPartnerResponse deliveryPartnerResponse = deliveryPartnerService.logoutDeliveryPartner(userRequest);

        if (deliveryPartnerResponse.isSuccess()) {
            return ResponseEntity.ok(deliveryPartnerResponse);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(deliveryPartnerResponse);
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<DeliveryPartnerResponse> deleteDeliveryPartner(@RequestBody DeleteDeliveryPartnerRequest deleteDeliveryPartnerRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        deleteDeliveryPartnerRequest.setEmail(email);

        DeliveryPartnerResponse deliveryPartnerResponse = deliveryPartnerService.deleteAccount(deleteDeliveryPartnerRequest);
        if (deliveryPartnerResponse.isSuccess()) {
            return ResponseEntity.ok(deliveryPartnerResponse);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(deliveryPartnerResponse);
        }
    }

    // TODO: Remove this route (ONLY FOR TESTING JWT AUTHORIZATION)
    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "This is a protected resource. You are authenticated!";
    }
}