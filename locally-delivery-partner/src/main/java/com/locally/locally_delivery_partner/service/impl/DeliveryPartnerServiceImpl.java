package com.locally.locally_delivery_partner.service.impl;

import com.locally.locally_delivery_partner.dto.*;
import com.locally.locally_delivery_partner.model.DeliveryPartner;
import com.locally.locally_delivery_partner.model.DeliveryPartnerRole;
import com.locally.locally_delivery_partner.model.VehicleDetails;
import com.locally.locally_delivery_partner.repository.DeliveryPartnerRepository;
import com.locally.locally_delivery_partner.repository.RoleRepository;
import com.locally.locally_delivery_partner.repository.VehicleDetailsRepository;
import com.locally.locally_delivery_partner.service.DeliveryPartnerService;
import com.locally.locally_delivery_partner.utils.DeliveryPartnerUtils;
import com.locally.locally_delivery_partner.utils.JwtUtil;
import com.locally.locally_delivery_partner.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryPartnerServiceImpl implements DeliveryPartnerService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private VehicleDetailsRepository vehicleDetailsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public DeliveryPartnerResponse createDeliveryPartner(CreateDeliveryPartnerRequest createDeliveryPartnerRequest) {
        /**
         * Create a new user and save it into the database
         */

        if (deliveryPartnerRepository.existsByEmail(createDeliveryPartnerRequest.getEmail())) {
            return DeliveryPartnerResponse.builder()
                    .responseCode(DeliveryPartnerUtils.ACCOUNT_EXISTS_CODE)
                    .success(DeliveryPartnerUtils.ACCOUNT_EXISTS_SUCCESS)
                    .responseMessage(DeliveryPartnerUtils.ACCOUNT_EXISTS_MESSAGE)
                    .build();
        }

        Optional<DeliveryPartnerRole> roleOptional = roleRepository.findByRoleName(DeliveryPartnerUtils.DELIVERY_PARTNER_ROLE_NAME);
        if (roleOptional.isEmpty()) {
            throw new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_ROLE_NOT_FOUND);
        }

        DeliveryPartnerRole userRole = roleOptional.get();

        DeliveryPartner deliveryPartner = DeliveryPartner.builder()
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
                .bankAccountNumber(createDeliveryPartnerRequest.getBankAccountNumber())
                .password(passwordEncoder.encode(createDeliveryPartnerRequest.getPassword()))
                .deliveryPartnerRole(userRole)
                .isActive(DeliveryPartnerUtils.DELIVERY_PARTNER_FALSE)
                .isVerified(DeliveryPartnerUtils.DELIVERY_PARTNER_FALSE)
                .build();

        deliveryPartnerRepository.save(deliveryPartner);

        // TODO: Implement EmailService and verification
        // Generate and store OTP
//        String otp = OtpUtil.generateOtp();
//        String otpKey = "verification:" + newUser.getEmail();
//        otpService.storeOtp(otpKey, otp, 900); // 15 minutes

//        // Send OTP via email
//        try {
//            emailService.sendVerificationOtp(newUser.getEmail(), otp);
//        } catch (Exception e) {
//            System.err.println("Failed to send OTP email: " + e.getMessage());
//        }
//        try {
//            emailService.sendWelcomeEmail(newUser.getEmail(), newUser.getFirstName());
//        } catch (Exception e) {
//            System.err.println("Failed to send welcome email: " + e.getMessage());
//        }

        String accessToken = jwtUtil.generateAccessToken(deliveryPartner.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(deliveryPartner.getEmail());

        return DeliveryPartnerResponse.builder()
                .responseCode(DeliveryPartnerUtils.ACCOUNT_CREATION_CODE)
                .success(DeliveryPartnerUtils.ACCOUNT_CREATION_SUCCESS)
                .responseMessage(DeliveryPartnerUtils.ACCOUNT_CREATION_MESSAGE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public DeliveryPartnerResponse createVehicle(CreateVehicleRequest createVehicleRequest) {
        /**
         * Create a new vehicle and save it into the database
         */

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(createVehicleRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        // Check how many vehicles already exist for this partner
        List<VehicleDetails> existingVehicles = vehicleDetailsRepository.findAllByDeliveryPartnerId(deliveryPartner.getId());

        if (vehicleDetailsRepository.findByLicenseNumber(createVehicleRequest.getLicenseNumber()).isPresent()) {
            throw new RuntimeException("Vehicle with this license number already exists.");
        }

        if (vehicleDetailsRepository.findByRegistrationNumber(createVehicleRequest.getRegistrationNumber()).isPresent()) {
            throw new RuntimeException("Vehicle with this registration number already exists.");
        }


        boolean isPrimary = existingVehicles.isEmpty();

        VehicleDetails vehicle = VehicleDetails.builder()
                .deliveryPartner(deliveryPartner)
                .vehicleType(createVehicleRequest.getVehicleType())
                .vehicleColor(createVehicleRequest.getVehicleColor())
                .vehicleModel(createVehicleRequest.getVehicleModel())
                .registrationNumber(createVehicleRequest.getRegistrationNumber())
                .licenseNumber(createVehicleRequest.getLicenseNumber())
                .isPrimaryVehicle(isPrimary)
                .build();

        vehicleDetailsRepository.save(vehicle);

        return DeliveryPartnerResponse.builder()
                .responseCode(DeliveryPartnerUtils.VEHICLE_CREATION_CODE)
                .success(DeliveryPartnerUtils.VEHICLE_CREATION_SUCCESS)
                .responseMessage(isPrimary ? DeliveryPartnerUtils.PRIMARY_VEHICLE_CREATION_MESSAGE : DeliveryPartnerUtils.SECONDARY_VEHICLE_CREATION_MESSAGE)
                .build();
    }

    @Override
    public DeliveryPartnerResponse loginDeliveryPartner(LoginRequest loginRequest) {
        /**
         * Log In the delivery partner and generate a JWT token
         */

        try {
            DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

            boolean isPasswordMatch = passwordEncoder.matches(loginRequest.getPassword(), deliveryPartner.getPassword());

            // TODO: Implement verification

//            if(!user.getIsVerified()) {
//                return AppResponse.builder()
//                        .responseCode(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
//                        .success(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
//                        .responseMessage(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
//                        .build();
//            }

            if (!isPasswordMatch) {
                return DeliveryPartnerResponse.builder()
                        .responseCode(DeliveryPartnerUtils.ACCOUNT_INCORRECT_PASSWORD_CODE)
                        .success(DeliveryPartnerUtils.ACCOUNT_INCORRECT_PASSWORD_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.ACCOUNT_INCORRECT_PASSWORD_MESSAGE)
                        .build();
            }

            String accessToken = jwtUtil.generateAccessToken(deliveryPartner.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(deliveryPartner.getEmail());

            return DeliveryPartnerResponse.builder()
                    .responseCode(DeliveryPartnerUtils.ACCOUNT_LOGIN_CODE)
                    .success(DeliveryPartnerUtils.ACCOUNT_LOGIN_SUCCESS)
                    .responseMessage(DeliveryPartnerUtils.ACCOUNT_LOGIN_MESSAGE)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DeliveryPartnerResponse logoutDeliveryPartner(RefreshTokenRequest logoutRequest) {
        /**
         * Log Out the delivery partner and expire the JWT token
         */

        String rawToken = logoutRequest.getRefreshToken();

        if (rawToken != null) {
            String refreshToken = rawToken.trim();

            if (!refreshToken.isEmpty()) {
                Date expirationDate = new Date(System.currentTimeMillis() + jwtUtil.getRefreshTokenExpirationTimeInMillis());

                redisUtil.blacklistToken(refreshToken, expirationDate);

                return DeliveryPartnerResponse.builder()
                        .responseCode(DeliveryPartnerUtils.LOGOUT_USER_CODE)
                        .success(DeliveryPartnerUtils.LOGOUT_USER_SUCCESS)
                        .responseMessage(DeliveryPartnerUtils.LOGOUT_USER_MESSAGE)
                        .accessToken(null)
                        .refreshToken(null)
                        .build();
            }
        }

        return DeliveryPartnerResponse.builder()
                .responseCode(DeliveryPartnerUtils.LOGOUT_USER_FAILED_CODE)
                .success(DeliveryPartnerUtils.LOGOUT_USER_FAILED_SUCCESS)
                .responseMessage(DeliveryPartnerUtils.LOGOUT_USER_FAILED_MESSAGE)
                .accessToken(null)
                .refreshToken(null)
                .build();
    }
}