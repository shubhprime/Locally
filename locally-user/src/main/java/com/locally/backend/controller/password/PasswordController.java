package com.locally.backend.controller.password;

import com.locally.backend.dto.*;
import com.locally.backend.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/password")
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/forgot-password")
    public AppResponse forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return passwordService.forgotPassword(request);
    }

    @PostMapping("/verify-otp")
    public AppResponse verifyOtp(@RequestBody OtpVerificationRequest request) {
        return passwordService.verifyOtp(request);
    }

    @PostMapping("/reset-password")
    public AppResponse resetPassword(@RequestBody ResetPasswordRequest request) {
        return passwordService.resetPassword(request);
    }

    @PostMapping("/change-password")
    public AppResponse changePassword(@RequestBody ChangePasswordRequest request) {
        return passwordService.changePassword(request);
    }
}