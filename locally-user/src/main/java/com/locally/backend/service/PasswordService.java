package com.locally.backend.service;

import com.locally.backend.dto.*;

public interface PasswordService {
    AppResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    AppResponse verifyOtp(OtpVerificationRequest otpVerificationRequest);

    AppResponse resetPassword(ResetPasswordRequest resetPasswordRequest);

    AppResponse changePassword(ChangePasswordRequest changePasswordRequest);
}