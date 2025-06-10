package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.*;

public interface PasswordService {
    public DeliveryPartnerResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    public DeliveryPartnerResponse verifyOtp(OtpVerificationRequest otpVerificationRequest);

    public DeliveryPartnerResponse resetPassword(ResetPasswordRequest resetPasswordRequest);

    public DeliveryPartnerResponse changePassword(ChangePasswordRequest changePasswordRequest);
}