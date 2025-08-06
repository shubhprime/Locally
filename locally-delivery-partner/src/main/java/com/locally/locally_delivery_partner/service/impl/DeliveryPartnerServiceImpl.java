package com.locally.locally_delivery_partner.service.impl;

import com.locally.locally_delivery_partner.dto.*;
import com.locally.locally_delivery_partner.model.DeliveryPartner;
import com.locally.locally_delivery_partner.model.Role;
import com.locally.locally_delivery_partner.model.VehicleDetails;
import com.locally.locally_delivery_partner.repository.DeliveryPartnerRepository;
import com.locally.locally_delivery_partner.repository.VehicleDetailsRepository;
import com.locally.locally_delivery_partner.service.DeliveryPartnerService;
import com.locally.locally_delivery_partner.service.DeliveryPartnerStatusService;
import com.locally.locally_delivery_partner.service.EmailService;
import com.locally.locally_delivery_partner.service.OtpService;
import com.locally.locally_delivery_partner.utils.DeliveryPartnerUtils;
import com.locally.locally_delivery_partner.utils.JwtUtil;
import com.locally.locally_delivery_partner.utils.OtpUtil;
import com.locally.locally_delivery_partner.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryPartnerServiceImpl implements DeliveryPartnerService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private DeliveryPartnerStatusService deliveryPartnerStatusService;

    @Autowired
    private VehicleDetailsRepository vehicleDetailsRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    // TODO: Anonymous user
    @Override
    public AppResponse<Void> createDeliveryPartner(CreateDeliveryPartnerRequest createDeliveryPartnerRequest) {
        /**
         * Create a new user and save it into the database
         */

        if (deliveryPartnerRepository.existsByEmail(createDeliveryPartnerRequest.getEmail())) {
            return AppResponse.<Void>builder()
                    .responseCode(DeliveryPartnerUtils.ACCOUNT_EXISTS_CODE)
                    .success(DeliveryPartnerUtils.ACCOUNT_EXISTS_SUCCESS)
                    .responseMessage(DeliveryPartnerUtils.ACCOUNT_EXISTS_MESSAGE)
                    .build();
        }

        Role userRole = Role.DELIVERY_PARTNER;

        DeliveryPartner newDeliveryPartner = DeliveryPartner.builder()
                .firstName(createDeliveryPartnerRequest.getFirstName())
                .lastName(createDeliveryPartnerRequest.getLastName())
                .gender(createDeliveryPartnerRequest.getGender())
                .state(createDeliveryPartnerRequest.getState())
                .country(createDeliveryPartnerRequest.getCountry())
                .address(createDeliveryPartnerRequest.getAddress())
                .email(createDeliveryPartnerRequest.getEmail())
                .phoneNumber(createDeliveryPartnerRequest.getPhoneNumber())
                .alternatePhoneNumber(createDeliveryPartnerRequest.getAlternatePhoneNumber())
                .modeOfDelivery(createDeliveryPartnerRequest.getModeOfDelivery())
                .governmentIdType(createDeliveryPartnerRequest.getGovernmentIdType())
                .governmentId(createDeliveryPartnerRequest.getGovernmentId())
                .accountHolderName(createDeliveryPartnerRequest.getAccountHolderName())
                .bankName(createDeliveryPartnerRequest.getBankName())
                .routingNumber(createDeliveryPartnerRequest.getRoutingNumber())
                .bankAccountNumber(createDeliveryPartnerRequest.getBankAccountNumber())
                .accountType(createDeliveryPartnerRequest.getAccountType())
                .isDeleted(false)
                .password(passwordEncoder.encode(createDeliveryPartnerRequest.getPassword()))
                .role(userRole)
                .isActive(DeliveryPartnerUtils.DELIVERY_PARTNER_FALSE)
                .isVerified(DeliveryPartnerUtils.DELIVERY_PARTNER_FALSE)
                .build();

        deliveryPartnerRepository.save(newDeliveryPartner);

        // Send OTP via email
        try {
            sendVerificationOtp(createDeliveryPartnerRequest.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
        try {
            emailService.sendWelcomeEmail(newDeliveryPartner.getEmail(), newDeliveryPartner.getFirstName());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        String accessToken = jwtUtil.generateAccessToken(newDeliveryPartner.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(newDeliveryPartner.getEmail());

        return AppResponse.<Void>builder()
                .responseCode(DeliveryPartnerUtils.ACCOUNT_CREATION_CODE)
                .success(DeliveryPartnerUtils.ACCOUNT_CREATION_SUCCESS)
                .responseMessage(DeliveryPartnerUtils.ACCOUNT_CREATION_MESSAGE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AppResponse<Void> uploadProfilePicture(UploadProfilePictureRequest uploadProfilePictureRequest) {
        /**
         * Upload user profile picture
         */

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(uploadProfilePictureRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        if (uploadProfilePictureRequest.getPicture() == null || uploadProfilePictureRequest.getPicture().isEmpty()) {
            throw new RuntimeException("No picture file provided.");
        }

        if (uploadProfilePictureRequest.getPicture().getSize() > 2 * 1024 * 1024) { // 2 MB
            throw new RuntimeException("File size too large. Max 2MB allowed.");
        }

        try {
            byte[] bytes = uploadProfilePictureRequest.getPicture().getBytes();
            String base64Encoded = Base64.getEncoder().encodeToString(bytes);
            deliveryPartner.setProfilePictureBase64(base64Encoded);

            deliveryPartnerRepository.save(deliveryPartner);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file");
        }

        return AppResponse.<Void>builder()
                .success(true)
                .responseCode("200")
                .responseMessage("Profile picture saved in DB.")
                .build();
    }

    @Override
    public AppResponse<DeliveryPartnerData> updateDeliveryPartnerProfile(UpdateDeliveryPartnerRequest updateDeliveryPartnerRequest) {
        /**
         * Update user profile
         */

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(updateDeliveryPartnerRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        if (updateDeliveryPartnerRequest.getFirstName() != null) deliveryPartner.setFirstName(updateDeliveryPartnerRequest.getFirstName());
        if (updateDeliveryPartnerRequest.getLastName() != null) deliveryPartner.setLastName(updateDeliveryPartnerRequest.getLastName());
        if (updateDeliveryPartnerRequest.getAddress() != null) deliveryPartner.setAddress(updateDeliveryPartnerRequest.getAddress());
        if (updateDeliveryPartnerRequest.getPhoneNumber() != null) deliveryPartner.setPhoneNumber(updateDeliveryPartnerRequest.getPhoneNumber());
        if (updateDeliveryPartnerRequest.getAlternatePhoneNumber() != null) deliveryPartner.setAlternatePhoneNumber(updateDeliveryPartnerRequest.getAlternatePhoneNumber());
        if (updateDeliveryPartnerRequest.getBankAccountNumber() != null) deliveryPartner.setBankAccountNumber(updateDeliveryPartnerRequest.getBankAccountNumber());
        if (updateDeliveryPartnerRequest.getCountry() != null) deliveryPartner.setCountry(updateDeliveryPartnerRequest.getCountry());
        if (updateDeliveryPartnerRequest.getState() != null) deliveryPartner.setState(updateDeliveryPartnerRequest.getState());

        deliveryPartnerRepository.save(deliveryPartner);

        DeliveryPartnerData data = DeliveryPartnerData.builder()
                .userId(deliveryPartner.getId().toString())
                .firstName(deliveryPartner.getFirstName())
                .lastName(deliveryPartner.getLastName())
                .email(deliveryPartner.getEmail())
                .build();

        return AppResponse.<DeliveryPartnerData>builder()
                .responseCode(DeliveryPartnerUtils.USER_PROFILE_UPDATE_CODE)
                .success(DeliveryPartnerUtils.USER_PROFILE_UPDATE_SUCCESS)
                .responseMessage(DeliveryPartnerUtils.USER_PROFILE_UPDATE_MESSAGE)
                .data(data)
                .build();
    }

    @Override
    public AppResponse<DeliveryPartnerData> loginDeliveryPartner(LoginRequest loginRequest) {
        /**
         * Log In the delivery partner and generate a JWT token
         */

        try {
            DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(loginRequest.getEmail()).orElse(null);

            if (deliveryPartner == null) {
                return AppResponse.<DeliveryPartnerData>builder()
                        .responseCode("400")
                        .success(false)
                        .responseMessage(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND)
                        .build();
            }

            boolean isPasswordMatch = passwordEncoder.matches(loginRequest.getPassword(), deliveryPartner.getPassword());

            // TODO: Implement verification

            if(!deliveryPartner.getIsVerified()) {
                return AppResponse.<DeliveryPartnerData>builder()
                        .responseCode(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
                        .success(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
                        .build();
            }

            if (!isPasswordMatch) {
                return AppResponse.<DeliveryPartnerData>builder()
                        .responseCode(DeliveryPartnerUtils.ACCOUNT_INCORRECT_PASSWORD_CODE)
                        .success(DeliveryPartnerUtils.ACCOUNT_INCORRECT_PASSWORD_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.ACCOUNT_INCORRECT_PASSWORD_MESSAGE)
                        .build();
            }

            // Reactivate soft-deleted user
            if (deliveryPartner.getIsDeleted()) {
                deliveryPartner.setIsDeleted(false);
                deliveryPartner.setDeletedAt(null);
                deliveryPartner.setIsActive(true);
                deliveryPartnerRepository.save(deliveryPartner);
            }

            String accessToken = jwtUtil.generateAccessToken(deliveryPartner.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(deliveryPartner.getEmail());

            // Mark the user as AVAILABLE upon login
            deliveryPartnerStatusService.markAvailable(deliveryPartner.getEmail());

            DeliveryPartnerData data = DeliveryPartnerData.builder()
                    .userId(deliveryPartner.getId().toString())
                    .firstName(deliveryPartner.getFirstName())
                    .lastName(deliveryPartner.getLastName())
                    .email(deliveryPartner.getEmail())
                    .build();

            return AppResponse.<DeliveryPartnerData>builder()
                    .responseCode(DeliveryPartnerUtils.ACCOUNT_LOGIN_CODE)
                    .success(DeliveryPartnerUtils.ACCOUNT_LOGIN_SUCCESS)
                    .responseMessage(DeliveryPartnerUtils.ACCOUNT_LOGIN_MESSAGE)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .data(data)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AppResponse<Void> logoutDeliveryPartner(RefreshTokenRequest logoutRequest) {
        /**
         * Log Out the delivery partner and expire the JWT token
         */

        String rawToken = logoutRequest.getRefreshToken();

        if (rawToken != null) {
            String refreshToken = rawToken.trim();

            if (!refreshToken.isEmpty()) {
                Date expirationDate = new Date(System.currentTimeMillis() + jwtUtil.getRefreshTokenExpirationTimeInMillis());

                redisUtil.blacklistToken(refreshToken, expirationDate);

                // Extract email and mark user UNAVAILABLE
                try {
                    String email = jwtUtil.retrieveSubject(refreshToken);
                    deliveryPartnerStatusService.markUnavailable(email);
                } catch (Exception e) {
                    return AppResponse.<Void>builder()
                            .responseCode(DeliveryPartnerUtils.LOGOUT_USER_FAILED_CODE)
                            .success(DeliveryPartnerUtils.LOGOUT_USER_FAILED_SUCCESS)
                            .responseMessage("Logout failed: invalid token")
                            .build();
                }

                return AppResponse.<Void>builder()
                        .responseCode(DeliveryPartnerUtils.LOGOUT_USER_CODE)
                        .success(DeliveryPartnerUtils.LOGOUT_USER_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.LOGOUT_USER_MESSAGE)
                        .accessToken(null)
                        .refreshToken(null)
                        .build();
            }
        }

        return AppResponse.<Void>builder()
                .responseCode(DeliveryPartnerUtils.LOGOUT_USER_FAILED_CODE)
                .success(DeliveryPartnerUtils.LOGOUT_USER_FAILED_SUCCESS)
                .responseMessage(DeliveryPartnerUtils.LOGOUT_USER_FAILED_MESSAGE)
                .accessToken(null)
                .refreshToken(null)
                .build();
    }

    @Override
    public AppResponse<Void> deleteAccount(DeleteDeliveryPartnerRequest deleteDeliveryPartnerRequest) {
        /**
         * Delete user account
         */

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(deleteDeliveryPartnerRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        boolean isPasswordMatch = passwordEncoder.matches(deleteDeliveryPartnerRequest.getPassword(), deliveryPartner.getPassword());

        if (!isPasswordMatch) {
            return AppResponse.<Void>builder()
                    .responseCode(DeliveryPartnerUtils.ACCOUNT_INCORRECT_PASSWORD_CODE)
                    .success(DeliveryPartnerUtils.ACCOUNT_INCORRECT_PASSWORD_SUCCESS)
                    .responseMessage(DeliveryPartnerUtils.ACCOUNT_INCORRECT_PASSWORD_MESSAGE)
                    .build();
        }

        // Soft Delete all vehicles associated with this delivery partner
        List<VehicleDetails> vehicles = vehicleDetailsRepository.findAllByDeliveryPartnerId(deliveryPartner.getId());
        for (VehicleDetails vehicle : vehicles) {
            vehicle.setIsDeleted(true);
            vehicle.setDeletedAt(LocalDateTime.now());
            vehicleDetailsRepository.save(vehicle);
        }

        // Soft Delete the delivery partner
        deliveryPartner.setIsDeleted(true);
        deliveryPartner.setDeletedAt(LocalDateTime.now());
        deliveryPartnerRepository.save(deliveryPartner);

        return AppResponse.<Void>builder()
                .success(true)
                .responseCode("200")
                .responseMessage("Account soft deleted successfully.")
                .build();
    }

    @Override
    public AppResponse<Void> sendVerificationOtp(SendVerificationOtpRequest sendVerificationOtpRequest) {
        /**
         * Send a verification OTP to the user's email
         */

        Optional<DeliveryPartner> userOptional = deliveryPartnerRepository.findByEmail(sendVerificationOtpRequest.getEmail());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        DeliveryPartner deliveryPartner = userOptional.get();

        if (deliveryPartner.getIsVerified()) {
            return AppResponse.<Void>builder()
                    .responseCode(DeliveryPartnerUtils.ACCOUNT_ALREADY_VERIFIED_CODE)
                    .success(DeliveryPartnerUtils.ACCOUNT_ALREADY_VERIFIED_SUCCESS)
                    .responseMessage(DeliveryPartnerUtils.ACCOUNT_ALREADY_VERIFIED_MESSAGE)
                    .build();
        }

        // Generate OTP
        String otp = OtpUtil.generateOtp();

        // Store OTP with a key, e.g., "verification:email@example.com" and expiry (e.g., 15 min = 900 seconds)
        String key = "verification:" + deliveryPartner.getEmail();
        otpService.storeOtp(key, otp, 900);

        emailService.sendVerificationOtp(deliveryPartner.getEmail(), otp);

        System.out.println("Verification OTP for user " + deliveryPartner.getEmail() + ": " + otp);

        return AppResponse.<Void>builder()
                .responseCode(DeliveryPartnerUtils.VERIFICATION_OTP_SENT_CODE)
                .success(DeliveryPartnerUtils.VERIFICATION_OTP_SENT_SUCCESS)
                .responseMessage(DeliveryPartnerUtils.VERIFICATION_OTP_SENT_MESSAGE)
                .build();
    }

    @Override
    public AppResponse<Void> sendVerificationOtp(String email) {
        Optional<DeliveryPartner> userOptional = deliveryPartnerRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        DeliveryPartner deliveryPartner = userOptional.get();

        if (deliveryPartner.getIsVerified()) {
            return AppResponse.<Void>builder()
                    .responseCode(DeliveryPartnerUtils.ACCOUNT_ALREADY_VERIFIED_CODE)
                    .success(DeliveryPartnerUtils.ACCOUNT_ALREADY_VERIFIED_SUCCESS)
                    .responseMessage(DeliveryPartnerUtils.ACCOUNT_ALREADY_VERIFIED_MESSAGE)
                    .build();
        }

        String otp = OtpUtil.generateOtp();
        String key = "verification:" + deliveryPartner.getEmail();
        otpService.storeOtp(key, otp, 900);

        emailService.sendVerificationOtp(deliveryPartner.getEmail(), otp);

        return AppResponse.<Void>builder()
                .responseCode(DeliveryPartnerUtils.VERIFICATION_OTP_SENT_CODE)
                .success(DeliveryPartnerUtils.VERIFICATION_OTP_SENT_SUCCESS)
                .responseMessage(DeliveryPartnerUtils.VERIFICATION_OTP_SENT_MESSAGE)
                .build();
    }

    @Override
    public AppResponse<Void> verifyVerificationOtp(OtpVerificationRequest otpVerificationRequest) {

        String otpKey = "verification:" + otpVerificationRequest.getEmail();
        boolean isValid = otpService.validateOtp(otpKey, otpVerificationRequest.getOtp());

        if (!isValid) {
            return AppResponse.<Void>builder()
                    .responseCode("400")
                    .success(false)
                    .responseMessage("Invalid or expired OTP")
                    .build();
        }

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(otpVerificationRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User Not Found. You Need To Sign Up First."));

        deliveryPartner.setIsVerified(true);
        deliveryPartnerRepository.save(deliveryPartner);

        redisUtil.delete(otpKey);

        return AppResponse.<Void>builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Email verified successfully")
                .build();
    }
}