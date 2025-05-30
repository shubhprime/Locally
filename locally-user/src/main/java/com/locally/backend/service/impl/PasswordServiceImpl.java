package com.locally.backend.service.impl;

import com.locally.backend.dto.*;
import com.locally.backend.model.User;
import com.locally.backend.repository.UserRepository;
import com.locally.backend.service.OtpService;
import com.locally.backend.service.PasswordService;
import com.locally.backend.service.UserCacheService;
import com.locally.backend.utils.AccountUtils;
import com.locally.backend.utils.JwtUtil;
import com.locally.backend.utils.OtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordServiceImpl implements PasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AppResponse forgotPassword(ForgotPasswordRequest passwordRequest) {
        /**
         * Send an OTP to the user via email or sms to make a new password
         */
        try {
            User user;

            if (("EMAIL").equals(passwordRequest.getRequestMedium())) {
                String email = passwordRequest.getRequestValue();
                user = userCacheService.getCachedUserByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User Not Found. You Need To Sign Up First"));

                if(!user.getIsVerified()) {
                    return AppResponse.builder()
                            .responseCode(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
                            .success(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
                            .responseMessage(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
                            .build();
                }

                String otp = OtpUtil.generateOtp();
                String key = "otp:" + email;
                otpService.storeOtp(key, otp, 900);

                System.out.println(otp);

                return AppResponse.builder()
                        .responseCode(AccountUtils.FORGOT_PASSWORD_EMAIL_CODE)
                        .success(AccountUtils.FORGOT_PASSWORD_EMAIL_SUCCESS)
                        .responseMessage(AccountUtils.FORGOT_PASSWORD_EMAIL_MESSAGE)
                        .build();
            }

            if (("PHONE_NUMBER").equals(passwordRequest.getRequestMedium())) {
                String phoneNumber = passwordRequest.getRequestValue();
                user = userCacheService.getCachedUserByPhone(phoneNumber)
                        .orElseThrow(() -> new RuntimeException("User Not Found. You Need To Sign Up First"));

                if(!user.getIsVerified()) {
                    return AppResponse.builder()
                            .responseCode(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
                            .success(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
                            .responseMessage(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
                            .build();
                }

                String otp = OtpUtil.generateOtp();
                String key = "otp:" + phoneNumber;
                otpService.storeOtp(key, otp, 900);

                return AppResponse.builder()
                        .responseCode(AccountUtils.FORGOT_PASSWORD_PHONE_NUMBER_CODE)
                        .success(AccountUtils.FORGOT_PASSWORD_PHONE_NUMBER_SUCCESS)
                        .responseMessage(AccountUtils.FORGOT_PASSWORD_PHONE_NUMBER_MESSAGE)
                        .build();
            }

            return AppResponse.builder()
                    .responseCode(AccountUtils.FORGOT_PASSWORD_FAILED_CODE)
                    .success(AccountUtils.FORGOT_PASSWORD_FAILED_SUCCESS)
                    .responseMessage(AccountUtils.FORGOT_PASSWORD_FAILED_MESSAGE)
                    .build();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AppResponse verifyOtp(OtpVerificationRequest otpVerificationRequest) {
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
                return AppResponse.builder()
                        .responseCode(AccountUtils.VERIFY_OTP_FAILED_CODE)
                        .success(AccountUtils.VERIFY_OTP_FAILED_SUCCESS)
                        .responseMessage(AccountUtils.VERIFY_OTP_FAILED_MESSAGE)
                        .build();
            }

            boolean isValid = otpService.validateOtp(key, otpVerificationRequest.getOtp());

            if (!isValid) {
                return AppResponse.builder()
                        .responseCode(AccountUtils.WRONG_OTP_CODE)
                        .success(AccountUtils.WRONG_OTP_SUCCESS)
                        .responseMessage(AccountUtils.WRONG_OTP_MESSAGE)
                        .build();
            }

            otpService.deleteOtp(key);

            return AppResponse.builder()
                    .responseCode(AccountUtils.FORGOT_PASSWORD_SUCCESS_CODE)
                    .success(AccountUtils.FORGOT_PASSWORD_SUCCESS_SUCCESS)
                    .responseMessage(AccountUtils.FORGOT_PASSWORD_SUCCESS_MESSAGE)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error verifying OTP", e);
        }
    }

    @Override
    public AppResponse resetPassword(ResetPasswordRequest resetPasswordRequest) {
        /**
         * Verifies the otp entered by the user
         */
        try {
            User user;
            if ("EMAIL".equals(resetPasswordRequest.getRequestMedium())) {
                user = userRepository.findByEmail(resetPasswordRequest.getRequestValue())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } else if ("PHONE_NUMBER".equals(resetPasswordRequest.getRequestMedium())) {
                user = userRepository.findByPhoneNumber(resetPasswordRequest.getRequestValue())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } else {
                return AppResponse.builder()
                        .responseCode(AccountUtils.RESET_PASSWORD_FAILED_CODE)
                        .success(AccountUtils.RESET_PASSWORD_FAILED_SUCCESS)
                        .responseMessage(AccountUtils.RESET_PASSWORD_FAILED_MESSAGE)
                        .build();
            }

            if(!user.getIsVerified()) {
                return AppResponse.builder()
                        .responseCode(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
                        .success(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
                        .responseMessage(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
                        .build();
            }

            // Set new password
            user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
            userRepository.save(user);

            return AppResponse.builder()
                    .responseCode(AccountUtils.RESET_PASSWORD_SUCCESS_CODE)
                    .success(AccountUtils.RESET_PASSWORD_SUCCESS_SUCCESS)
                    .responseMessage(AccountUtils.RESET_PASSWORD_SUCCESS_MESSAGE)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AppResponse changePassword(ChangePasswordRequest changePasswordRequest) {
        /**
         * Validates old password with the entered password and
         */
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(currentUsername)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if(!user.getIsVerified()) {
                return AppResponse.builder()
                        .responseCode(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
                        .success(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
                        .responseMessage(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
                        .build();
            }

            // Validate old password
            if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
                return AppResponse.builder()
                        .responseCode(AccountUtils.CHANGE_PASSWORD_FAILED_CODE)
                        .success(AccountUtils.CHANGE_PASSWORD_FAILED_SUCCESS)
                        .responseMessage(AccountUtils.CHANGE_PASSWORD_FAILED_MESSAGE)
                        .build();
            }

            // Set new password
            user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
            userRepository.save(user);

            // Generate new tokens after password change
            String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            return AppResponse.builder()
                    .responseCode(AccountUtils.CHANGE_PASSWORD_SUCCESS_CODE)
                    .success(AccountUtils.CHANGE_PASSWORD_SUCCESS_SUCCESS)
                    .responseMessage(AccountUtils.CHANGE_PASSWORD_SUCCESS_MESSAGE)
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}