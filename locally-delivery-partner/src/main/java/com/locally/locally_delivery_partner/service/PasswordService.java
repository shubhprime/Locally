package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.*;

public interface PasswordService {
    AppResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    AppResponse verifyOtp(OtpVerificationRequest otpVerificationRequest);

    AppResponse resetPassword(ResetPasswordRequest resetPasswordRequest);

    AppResponse changePassword(ChangePasswordRequest changePasswordRequest);
}