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
    public ResponseEntity<AppResponse> createUser(@RequestBody CreateDeliveryPartnerRequest createDeliveryPartnerRequest) {
        AppResponse appResponse = deliveryPartnerService.createDeliveryPartner(createDeliveryPartnerRequest);

        if (appResponse.getResponseCode().equals(DeliveryPartnerUtils.ACCOUNT_EXISTS_CODE)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(appResponse); // 409 Conflict if account exists
        }

        if (appResponse.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(appResponse); // 201 Created
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(appResponse); // Generic fallback
        }
    }

    @PostMapping("/send-verification-otp")
    public ResponseEntity<AppResponse> sendVerificationOtp(@RequestBody SendVerificationOtpRequest sendVerificationOtpRequest) {
        AppResponse appResponse = deliveryPartnerService.sendVerificationOtp(sendVerificationOtpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(appResponse);
    }

    @PostMapping("/verify-verification-otp")
    public ResponseEntity<AppResponse> verifyVerificationOtp(@RequestBody OtpVerificationRequest request) {
        AppResponse appResponse = deliveryPartnerService.verifyVerificationOtp(request);
        return ResponseEntity.status(HttpStatus.OK).body(appResponse);
    }

    @PostMapping("/upload-profile-picture")
    public ResponseEntity<AppResponse> uploadProfilePicture(@RequestParam("picture") MultipartFile picture, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        UploadProfilePictureRequest uploadProfilePictureRequest = new UploadProfilePictureRequest(email, picture);

        AppResponse appResponse = deliveryPartnerService.uploadProfilePicture(uploadProfilePictureRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(appResponse);
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<AppResponse> updateProfile(@RequestBody UpdateDeliveryPartnerRequest updateDeliveryPartnerRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        updateDeliveryPartnerRequest.setEmail(email);

        AppResponse appResponse = deliveryPartnerService.updateDeliveryPartnerProfile(updateDeliveryPartnerRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(appResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AppResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        AppResponse appResponse = deliveryPartnerService.loginDeliveryPartner(loginRequest);

        if (appResponse.isSuccess()) {
            return ResponseEntity.ok(appResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(appResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AppResponse> logoutUser(@RequestBody RefreshTokenRequest userRequest) {
        AppResponse appResponse = deliveryPartnerService.logoutDeliveryPartner(userRequest);

        if (appResponse.isSuccess()) {
            return ResponseEntity.ok(appResponse);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(appResponse);
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<AppResponse> deleteDeliveryPartner(@RequestBody DeleteDeliveryPartnerRequest deleteDeliveryPartnerRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        deleteDeliveryPartnerRequest.setEmail(email);

        AppResponse appResponse = deliveryPartnerService.deleteAccount(deleteDeliveryPartnerRequest);
        if (appResponse.isSuccess()) {
            return ResponseEntity.ok(appResponse);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(appResponse);
        }
    }

    // TODO: Remove this route (ONLY FOR TESTING JWT AUTHORIZATION)
    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "This is a protected resource. You are authenticated!";
    }
}