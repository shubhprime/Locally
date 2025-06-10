package com.locally.backend.controller.security;

import com.locally.backend.dto.AppResponse;
import com.locally.backend.dto.RefreshTokenRequest;
import com.locally.backend.dto.TokenResponse;
import com.locally.backend.utils.AccountUtils;
import com.locally.backend.utils.JwtUtil;
import com.locally.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: Implement refresh token validation
@RestController
@RequestMapping("/api/user/v1/token")
public class TokenController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            // Check if refresh token is blacklisted
            if (redisUtil.isTokenBlacklisted(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AppResponse.builder()
                                .responseCode(AccountUtils.REFRESH_TOKEN_BLACKLISTED_CODE)
                                .success(AccountUtils.REFRESH_TOKEN_BLACKLISTED_SUCCESS)
                                .responseMessage(AccountUtils.REFRESH_TOKEN_BLACKLISTED_MESSAGE)
                                .accessToken(null)
                                .refreshToken(null)
                                .build());
            }


            // Check expiration BEFORE parsing the subject
            if (jwtUtil.isRefreshTokenExpired(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AppResponse.builder()
                                .responseCode(AccountUtils.REFRESH_TOKEN_EXPIRED_CODE)
                                .success(AccountUtils.REFRESH_TOKEN_EXPIRED_SUCCESS)
                                .responseMessage(AccountUtils.REFRESH_TOKEN_EXPIRED_MESSAGE)
                                .accessToken(null)
                                .refreshToken(null)
                                .build()
                        );
            }

            // Generate new tokens
            String email = jwtUtil.retrieveSubject(refreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(email);
            String newRefreshToken = jwtUtil.generateRefreshToken(email);

            redisUtil.blacklistToken(refreshToken, jwtUtil.getExpirationDate(refreshToken));

            TokenResponse tokenResponse = new TokenResponse(newAccessToken, newRefreshToken);
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AppResponse.builder()
                            .responseCode(AccountUtils.INVALID_REFRESH_TOKEN_CODE)
                            .success(AccountUtils.INVALID_REFRESH_TOKEN_SUCCESS)
                            .responseMessage(AccountUtils.INVALID_REFRESH_TOKEN_MESSAGE)
                            .accessToken(null)
                            .refreshToken(null)
                            .build()
                    );
        }
    }
}