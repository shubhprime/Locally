package com.locally.backend.service.impl;

import com.locally.backend.dto.*;
import com.locally.backend.model.Role;
import com.locally.backend.model.User;
import com.locally.backend.repository.RoleRepository;
import com.locally.backend.repository.UserRepository;
import com.locally.backend.service.EmailService;
import com.locally.backend.service.OtpService;
import com.locally.backend.service.UserService;
import com.locally.backend.utils.AccountUtils;
import com.locally.backend.utils.JwtUtil;
import com.locally.backend.utils.OtpUtil;
import com.locally.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public AppResponse createUser(UserRequest userRequest) {
        /**
         * Create a new user and save it into the database
         */

        if(userRepository.existsByEmail(userRequest.getEmail())) {
            return AppResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .success(AccountUtils.ACCOUNT_EXISTS_SUCCESS)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .build();
        }

        Optional<Role> roleOptional = roleRepository.findByRoleName("USER");
        if(roleOptional.isEmpty()) {
            throw new RuntimeException(AccountUtils.USER_ROLE_NOT_FOUND);
        }

        Role userRole = roleOptional.get();

        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .gender(userRequest.getGender())
                .state(userRequest.getState())
                .country(userRequest.getCountry())
                .address(userRequest.getAddress())
                .email(userRequest.getEmail())
                .phoneNumber(userRequest.getPhoneNumber())
                .alternatePhoneNumber(userRequest.getAlternatePhoneNumber())
                .isDeleted(false)
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .isVerified(false)
                .role(userRole)
                .build();

        userRepository.save(newUser);

        // Send OTP via email
        try {
            sendVerificationOtp(userRequest.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
        try {
            emailService.sendWelcomeEmail(newUser.getEmail(), newUser.getFirstName());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        String accessToken = jwtUtil.generateAccessToken(newUser.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(newUser.getEmail());

        return AppResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_CODE)
                .success(AccountUtils.ACCOUNT_CREATION_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_MESSAGE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AppResponse uploadProfilePicture(UploadProfilePictureRequest uploadProfilePictureRequest) {
        /**
         * Upload user profile picture
         */

        User user = userRepository.findByEmailAndIsDeletedFalse(uploadProfilePictureRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        if (uploadProfilePictureRequest.getPicture() == null || uploadProfilePictureRequest.getPicture().isEmpty()) {
            throw new RuntimeException(AccountUtils.PICTURE_FILE_NOT_FOUND);
        }

        if (uploadProfilePictureRequest.getPicture().getSize() > 2 * 1024 * 1024) { // 2 MB
            throw new RuntimeException(AccountUtils.PICTURE_FILE_TOO_LARGE);
        }

        try {
            byte[] bytes = uploadProfilePictureRequest.getPicture().getBytes();
            String base64Encoded = Base64.getEncoder().encodeToString(bytes);
            user.setProfilePictureBase64(base64Encoded);

            userRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException(AccountUtils.ERROR_READING_FILE);
        }

        return AppResponse.builder()
                .responseCode(AccountUtils.PICTURE_SAVED_CODE)
                .success(AccountUtils.PICTURE_SAVED_SUCCESS)
                .responseMessage(AccountUtils.PICTURE_SAVED_MESSAGE)
                .build();
    }

    @Override
    public AppResponse updateUserProfile(UpdateUserRequest updateUserRequest) {
        /**
         * Update user profile
         */

        User user = userRepository.findByEmailAndIsDeletedFalse(updateUserRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        if (updateUserRequest.getFirstName() != null) user.setFirstName(updateUserRequest.getFirstName());
        if (updateUserRequest.getLastName() != null) user.setLastName(updateUserRequest.getLastName());
        if (updateUserRequest.getAddress() != null) user.setAddress(updateUserRequest.getAddress());
        if (updateUserRequest.getPhoneNumber() != null) user.setPhoneNumber(updateUserRequest.getPhoneNumber());
        if (updateUserRequest.getAlternatePhoneNumber() != null) user.setAlternatePhoneNumber(updateUserRequest.getAlternatePhoneNumber());
        if (updateUserRequest.getCountry() != null) user.setCountry(updateUserRequest.getCountry());
        if (updateUserRequest.getState() != null) user.setState(updateUserRequest.getState());

        userRepository.save(user);

        return AppResponse.builder()
                .responseCode(AccountUtils.USER_PROFILE_UPDATE_CODE)
                .success(AccountUtils.USER_PROFILE_UPDATE_SUCCESS)
                .responseMessage(AccountUtils.USER_PROFILE_UPDATE_MESSAGE)
                .build();
    }

    @Override
    public AppResponse loginUser(LoginRequest loginRequest) {
        /**
         * Log In the user and generate a JWT token
         */

        try {
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            boolean isPasswordMatch = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());

            // TODO: Implement verification

            if(!user.getIsVerified()) {
                return AppResponse.builder()
                        .responseCode(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
                        .success(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
                        .responseMessage(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
                        .build();
            }

            if(!isPasswordMatch) {
                return AppResponse.builder()
                        .responseCode(AccountUtils.ACCOUNT_INCORRECT_PASSWORD_CODE)
                        .success(AccountUtils.ACCOUNT_INCORRECT_PASSWORD_SUCCESS)
                        .responseMessage(AccountUtils.ACCOUNT_INCORRECT_PASSWORD_MESSAGE)
                        .build();
            }

            // Reactivate soft-deleted user
            if (user.getIsDeleted()) {
                user.setIsDeleted(false);
                user.setDeletedAt(null);
                userRepository.save(user);
            }

            String accessToken = jwtUtil.generateAccessToken(user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            return AppResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_LOGIN_CODE)
                    .success(AccountUtils.ACCOUNT_LOGIN_SUCCESS)
                    .responseMessage(AccountUtils.ACCOUNT_LOGIN_MESSAGE)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    @Override
    public AppResponse logoutUser(RefreshTokenRequest logoutRequest) {
        /**
         * Log Out the user and expire the JWT token
         */

        String rawToken = logoutRequest.getRefreshToken();

        if (rawToken != null) {
            String refreshToken = rawToken.trim();

            if (!refreshToken.isEmpty()) {
                Date expirationDate = new Date(System.currentTimeMillis() + jwtUtil.getRefreshTokenExpirationTimeInMillis());

                redisUtil.blacklistToken(refreshToken, expirationDate);

                return AppResponse.builder()
                        .responseCode(AccountUtils.LOGOUT_USER_CODE)
                        .success(AccountUtils.LOGOUT_USER_SUCCESS)
                        .responseMessage(AccountUtils.LOGOUT_USER_MESSAGE)
                        .accessToken(null)
                        .refreshToken(null)
                        .build();
            }
        }

        return AppResponse.builder()
                .responseCode(AccountUtils.LOGOUT_USER_FAILED_CODE)
                .success(AccountUtils.LOGOUT_USER_FAILED_SUCCESS)
                .responseMessage(AccountUtils.LOGOUT_USER_FAILED_MESSAGE)
                .accessToken(null)
                .refreshToken(null)
                .build();
    }

    @Override
    public AppResponse deleteAccount(DeleteUserRequest deleteUserRequest) {
        /**
         * Delete user account
         */

        User user = userRepository.findByEmailAndIsDeletedFalse(deleteUserRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        boolean isPasswordMatch = passwordEncoder.matches(deleteUserRequest.getPassword(), user.getPassword());

        if (!isPasswordMatch) {
            return AppResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_INCORRECT_PASSWORD_CODE)
                    .success(AccountUtils.ACCOUNT_INCORRECT_PASSWORD_SUCCESS)
                    .responseMessage(AccountUtils.ACCOUNT_INCORRECT_PASSWORD_MESSAGE)
                    .build();
        }

        // Soft Delete the user
        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        return AppResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_SOFT_DELETED_CODE)
                .success(AccountUtils.ACCOUNT_SOFT_DELETED_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_SOFT_DELETED_MESSAGE)
                .build();
    }

    @Override
    public AppResponse sendVerificationOtp(SendVerificationOtpRequest sendVerificationOtpRequest) {
        /**
         * Send a verification OTP to the user's email
         */

        Optional<User> userOptional = userRepository.findByEmail(sendVerificationOtpRequest.getEmail());

        if (userOptional.isEmpty()) {
            throw new RuntimeException(AccountUtils.USER_NOT_FOUND);
        }

        User user = userOptional.get();

        if (user.getIsVerified()) {
            return AppResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_ALREADY_VERIFIED_CODE)
                    .success(AccountUtils.ACCOUNT_ALREADY_VERIFIED_SUCCESS)
                    .responseMessage(AccountUtils.ACCOUNT_ALREADY_VERIFIED_MESSAGE)
                    .build();
        }

        // Generate OTP
        String otp = OtpUtil.generateOtp();

        // Store OTP with a key, e.g., "verification:email@example.com" and expiry (e.g., 15 min = 900 seconds)
        String key = "verification:" + user.getEmail();
        otpService.storeOtp(key, otp, 900);

        emailService.sendVerificationOtp(user.getEmail(), otp);

        System.out.println("Verification OTP for user " + user.getEmail() + ": " + otp);

        return AppResponse.builder()
                .responseCode(AccountUtils.VERIFICATION_OTP_SENT_CODE)
                .success(AccountUtils.VERIFICATION_OTP_SENT_SUCCESS)
                .responseMessage(AccountUtils.VERIFICATION_OTP_SENT_MESSAGE)
                .build();
    }

    @Override
    public AppResponse sendVerificationOtp(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new RuntimeException(AccountUtils.USER_NOT_FOUND);
        }

        User user = userOptional.get();

        if (user.getIsVerified()) {
            return AppResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_ALREADY_VERIFIED_CODE)
                    .success(AccountUtils.ACCOUNT_ALREADY_VERIFIED_SUCCESS)
                    .responseMessage(AccountUtils.ACCOUNT_ALREADY_VERIFIED_MESSAGE)
                    .build();
        }

        String otp = OtpUtil.generateOtp();
        String key = "verification:" + user.getEmail();
        otpService.storeOtp(key, otp, 900);

        emailService.sendVerificationOtp(user.getEmail(), otp);

        return AppResponse.builder()
                .responseCode(AccountUtils.VERIFICATION_OTP_SENT_CODE)
                .success(AccountUtils.VERIFICATION_OTP_SENT_SUCCESS)
                .responseMessage(AccountUtils.VERIFICATION_OTP_SENT_MESSAGE)
                .build();
    }

    @Override
    public AppResponse verifyVerificationOtp(OtpVerificationRequest otpVerificationRequest) {

        String otpKey = "verification:" + otpVerificationRequest.getEmail();
        boolean isValid = otpService.validateOtp(otpKey, otpVerificationRequest.getOtp());

        if (!isValid) {
            return AppResponse.builder()
                    .responseCode(AccountUtils.WRONG_OTP_CODE)
                    .success(AccountUtils.WRONG_OTP_SUCCESS)
                    .responseMessage(AccountUtils.WRONG_OTP_MESSAGE)
                    .build();
        }

        User user = userRepository.findByEmail(otpVerificationRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

        user.setIsVerified(true);
        userRepository.save(user);

        redisUtil.delete(otpKey);

        return AppResponse.builder()
                .responseCode(AccountUtils.EMAIL_VERIFICATION_SUCCESS_CODE)
                .success(AccountUtils.EMAIL_VERIFICATION_SUCCESS_SUCCESS)
                .responseMessage(AccountUtils.EMAIL_VERIFICATION_SUCCESS_MESSAGE)
                .build();
    }
}