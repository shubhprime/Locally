package com.locally.locally_delivery_partner.service.impl;

import com.locally.locally_delivery_partner.dto.*;
import com.locally.locally_delivery_partner.model.DeliveryPartner;
import com.locally.locally_delivery_partner.repository.DeliveryPartnerRepository;
import com.locally.locally_delivery_partner.service.OtpService;
import com.locally.locally_delivery_partner.service.PasswordService;
import com.locally.locally_delivery_partner.utils.DeliveryPartnerUtils;
import com.locally.locally_delivery_partner.utils.JwtUtil;
import com.locally.locally_delivery_partner.utils.OtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordServiceImpl implements PasswordService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DeliveryPartnerCacheService deliveryPartnerCacheService;

    @Override
    public DeliveryPartnerResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        /**
         * Send an OTP to the user via email or sms to make a new password
         */
        try {
            DeliveryPartner deliveryPartner;

            if (("EMAIL").equals(forgotPasswordRequest.getRequestMedium())) {
                String email = forgotPasswordRequest.getRequestValue();
                deliveryPartner = deliveryPartnerCacheService.getCachedDeliveryPartnerByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User Not Found. You Need To Sign Up First"));

                if(!deliveryPartner.getIsVerified()) {
                    return DeliveryPartnerResponse.builder()
                            .responseCode(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
                            .success(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
                            .responseMessage(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
                            .build();
                }

                String otp = OtpUtil.generateOtp();
                String key = "otp:" + email;
                otpService.storeOtp(key, otp, 900);

                System.out.println(otp);

                return DeliveryPartnerResponse.builder()
                        .responseCode(DeliveryPartnerUtils.FORGOT_PASSWORD_EMAIL_CODE)
                        .success(DeliveryPartnerUtils.FORGOT_PASSWORD_EMAIL_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.FORGOT_PASSWORD_EMAIL_MESSAGE)
                        .build();
            }

            if (("PHONE_NUMBER").equals(forgotPasswordRequest.getRequestMedium())) {
                String phoneNumber = forgotPasswordRequest.getRequestValue();
                deliveryPartner = deliveryPartnerCacheService.getCachedDeliveryPartnerByPhone(phoneNumber)
                        .orElseThrow(() -> new RuntimeException("User Not Found. You Need To Sign Up First"));

                if(!deliveryPartner.getIsVerified()) {
                    return DeliveryPartnerResponse.builder()
                            .responseCode(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
                            .success(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
                            .responseMessage(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
                            .build();
                }

                String otp = OtpUtil.generateOtp();
                String key = "otp:" + phoneNumber;
                otpService.storeOtp(key, otp, 900);

                return DeliveryPartnerResponse.builder()
                        .responseCode(DeliveryPartnerUtils.FORGOT_PASSWORD_PHONE_NUMBER_CODE)
                        .success(DeliveryPartnerUtils.FORGOT_PASSWORD_PHONE_NUMBER_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.FORGOT_PASSWORD_PHONE_NUMBER_MESSAGE)
                        .build();
            }

            return DeliveryPartnerResponse.builder()
                    .responseCode(DeliveryPartnerUtils.FORGOT_PASSWORD_FAILED_CODE)
                    .success(DeliveryPartnerUtils.FORGOT_PASSWORD_FAILED_SUCCESS)
                    .responseMessage(DeliveryPartnerUtils.FORGOT_PASSWORD_FAILED_MESSAGE)
                    .build();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DeliveryPartnerResponse verifyOtp(OtpVerificationRequest otpVerificationRequest) {
        /**
         * Verifies the otp entered by the user
         */
        try {
            String key;
            if ("EMAIL".equals(otpVerificationRequest.getRequestMedium())) {
                key = "otp:" + otpVerificationRequest.getRequestValue();
            } else if ("PHONE_NUMBER".equals(otpVerificationRequest.getRequestMedium())) {
                key = "otp:" + otpVerificationRequest.getRequestValue();
            } else {
                return DeliveryPartnerResponse.builder()
                        .responseCode(DeliveryPartnerUtils.VERIFY_OTP_FAILED_CODE)
                        .success(DeliveryPartnerUtils.VERIFY_OTP_FAILED_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.VERIFY_OTP_FAILED_MESSAGE)
                        .build();
            }

            boolean isValid = otpService.validateOtp(key, otpVerificationRequest.getOtp());

            if (!isValid) {
                return DeliveryPartnerResponse.builder()
                        .responseCode(DeliveryPartnerUtils.WRONG_OTP_CODE)
                        .success(DeliveryPartnerUtils.WRONG_OTP_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.WRONG_OTP_MESSAGE)
                        .build();
            }

            otpService.deleteOtp(key);

            return DeliveryPartnerResponse.builder()
                    .responseCode(DeliveryPartnerUtils.FORGOT_PASSWORD_SUCCESS_CODE)
                    .success(DeliveryPartnerUtils.FORGOT_PASSWORD_SUCCESS_SUCCESS)
                    .responseMessage(DeliveryPartnerUtils.FORGOT_PASSWORD_SUCCESS_MESSAGE)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error verifying OTP", e);
        }
    }

    @Override
    public DeliveryPartnerResponse resetPassword(ResetPasswordRequest resetPasswordRequest) {
        /**
         * Verifies the otp entered by the user
         */
        try {
            DeliveryPartner deliveryPartner;
            if ("EMAIL".equals(resetPasswordRequest.getRequestMedium())) {
                deliveryPartner = deliveryPartnerRepository.findByEmail(resetPasswordRequest.getRequestValue())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } else if ("PHONE_NUMBER".equals(resetPasswordRequest.getRequestMedium())) {
                deliveryPartner = deliveryPartnerRepository.findByPhoneNumber(resetPasswordRequest.getRequestValue())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } else {
                return DeliveryPartnerResponse.builder()
                        .responseCode(DeliveryPartnerUtils.RESET_PASSWORD_FAILED_CODE)
                        .success(DeliveryPartnerUtils.RESET_PASSWORD_FAILED_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.RESET_PASSWORD_FAILED_MESSAGE)
                        .build();
            }

            if(!deliveryPartner.getIsVerified()) {
                return DeliveryPartnerResponse.builder()
                        .responseCode(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
                        .success(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
                        .build();
            }

            // Set new password
            deliveryPartner.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
            deliveryPartnerRepository.save(deliveryPartner);

            return DeliveryPartnerResponse.builder()
                    .responseCode(DeliveryPartnerUtils.RESET_PASSWORD_SUCCESS_CODE)
                    .success(DeliveryPartnerUtils.RESET_PASSWORD_SUCCESS_SUCCESS)
                    .responseMessage(DeliveryPartnerUtils.RESET_PASSWORD_SUCCESS_MESSAGE)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DeliveryPartnerResponse changePassword(ChangePasswordRequest changePasswordRequest) {
        /**
         * Validates old password with the entered password and
         */
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(currentUsername)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if(!deliveryPartner.getIsVerified()) {
                return DeliveryPartnerResponse.builder()
                        .responseCode(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
                        .success(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
                        .build();
            }

            // Validate old password
            if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), deliveryPartner.getPassword())) {
                return DeliveryPartnerResponse.builder()
                        .responseCode(DeliveryPartnerUtils.CHANGE_PASSWORD_FAILED_CODE)
                        .success(DeliveryPartnerUtils.CHANGE_PASSWORD_FAILED_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.CHANGE_PASSWORD_FAILED_MESSAGE)
                        .build();
            }

            // Set new password
            deliveryPartner.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
            deliveryPartnerRepository.save(deliveryPartner);

            // Generate new tokens after password change
            String newAccessToken = jwtUtil.generateAccessToken(deliveryPartner.getEmail());
            String newRefreshToken = jwtUtil.generateRefreshToken(deliveryPartner.getEmail());

            return DeliveryPartnerResponse.builder()
                    .responseCode(DeliveryPartnerUtils.CHANGE_PASSWORD_SUCCESS_CODE)
                    .success(DeliveryPartnerUtils.CHANGE_PASSWORD_SUCCESS_SUCCESS)
                    .responseMessage(DeliveryPartnerUtils.CHANGE_PASSWORD_SUCCESS_MESSAGE)
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}