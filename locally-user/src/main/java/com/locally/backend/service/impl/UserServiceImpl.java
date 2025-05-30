package com.locally.backend.service.impl;

import com.locally.backend.dto.AppResponse;
import com.locally.backend.dto.UserRequest;
import com.locally.backend.model.Role;
import com.locally.backend.model.User;
import com.locally.backend.repository.RoleRepository;
import com.locally.backend.repository.UserRepository;
// TODO: Implement EmailService
//import com.locally.backend.service.EmailService;
import com.locally.backend.service.OtpService;
import com.locally.backend.service.UserService;
import com.locally.backend.utils.AccountUtils;
import com.locally.backend.utils.JwtUtil;
import com.locally.backend.utils.OtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // TODO: Implement EmailService
//    @Autowired
////    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

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
            throw new RuntimeException("USER role not found");
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
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .isVerified(false)
                .role(userRole)
                .build();

        userRepository.save(newUser);

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

    // TODO: Implement validation

    @Override
    public AppResponse sendVerificationOtp(UserRequest userRequest) {
        /**
         * Send a verification OTP to the user's email
         */

        Optional<User> userOptional = userRepository.findByEmail(userRequest.getEmail());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
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

        // TODO: Send the OTP via email (call your email service here)

        System.out.println("Verification OTP for user " + user.getEmail() + ": " + otp);

        return AppResponse.builder()
                .responseCode(AccountUtils.VERIFICATION_OTP_SENT_CODE)
                .success(AccountUtils.VERIFICATION_OTP_SENT_SUCCESS)
                .responseMessage(AccountUtils.VERIFICATION_OTP_SENT_MESSAGE)
                .build();
    }
}