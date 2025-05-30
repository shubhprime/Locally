package com.locally.backend.service.impl;

import com.locally.backend.dto.AppResponse;
import com.locally.backend.dto.LoginRequest;
import com.locally.backend.model.User;
import com.locally.backend.repository.UserRepository;
import com.locally.backend.service.LoginService;
import com.locally.backend.utils.AccountUtils;
import com.locally.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AppResponse loginUser(LoginRequest loginRequest) {
        /**
         * Log In the user and generate a JWT token
         */

        try {
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User Not Found. You Need To Sign Up First."));

            boolean isPasswordMatch = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());

            // TODO: Implement verification

//            if(!user.getIsVerified()) {
//                return AppResponse.builder()
//                        .responseCode(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_CODE)
//                        .success(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_SUCCESS)
//                        .responseMessage(AccountUtils.ACCOUNT_IS_VERIFIED_FAILED_MESSAGE)
//                        .build();
//            }

            if(!isPasswordMatch) {
                return AppResponse.builder()
                        .responseCode(AccountUtils.ACCOUNT_INCORRECT_PASSWORD_CODE)
                        .success(AccountUtils.ACCOUNT_INCORRECT_PASSWORD_SUCCESS)
                        .responseMessage(AccountUtils.ACCOUNT_INCORRECT_PASSWORD_MESSAGE)
                        .build();
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
}
