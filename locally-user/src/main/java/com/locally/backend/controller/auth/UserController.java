package com.locally.backend.controller.auth;

import com.locally.backend.dto.AppResponse;
import com.locally.backend.dto.LoginRequest;
import com.locally.backend.dto.RefreshTokenRequest;
import com.locally.backend.dto.UserRequest;
import com.locally.backend.service.LoginService;
import com.locally.backend.service.UserService;
import com.locally.backend.utils.AccountUtils;
import com.locally.backend.utils.JwtUtil;
import com.locally.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/user/v1/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/signup")
    public AppResponse createUser(@RequestBody UserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<AppResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        AppResponse appResponse = loginService.loginUser(loginRequest);

        if (appResponse.isSuccess()) {

            // Build response with both tokens
            AppResponse responseWithTokens = AppResponse.builder()
                    .responseCode(appResponse.getResponseCode())
                    .success(true)
                    .responseMessage(appResponse.getResponseMessage())
                    .accessToken(appResponse.getAccessToken())           // access token
                    .refreshToken(appResponse.getRefreshToken())         // refresh token
                    .build();

            return ResponseEntity.ok(responseWithTokens);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(appResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<AppResponse> logoutUser(@RequestBody RefreshTokenRequest userRequest) {
        String refreshToken = userRequest.getRefreshToken();

        if(refreshToken != null && !refreshToken.isEmpty()) {
            Date expirationDate = new Date(System.currentTimeMillis() + jwtUtil.getRefreshTokenExpirationTimeInMillis());

            redisUtil.blacklistToken(refreshToken, expirationDate);

            return ResponseEntity.ok(AppResponse.builder()
                    .responseCode(AccountUtils.LOGOUT_USER_CODE)
                    .success(AccountUtils.LOGOUT_USER_SUCCESS)
                    .responseMessage(AccountUtils.LOGOUT_USER_MESSAGE)
                    .accessToken(null)
                    .refreshToken(null)
                    .build()
            );
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AppResponse.builder()
                .responseCode(AccountUtils.LOGOUT_USER_FAILED_CODE)
                .success(AccountUtils.LOGOUT_USER_FAILED_SUCCESS)
                .responseMessage(AccountUtils.LOGOUT_USER_FAILED_MESSAGE)
                .accessToken(null)
                .refreshToken(null)
                .build());
    }

    // TODO: Remove this route (ONLY FOR TESTING JWT AUTHORIZATION)
    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "This is a protected resource. You are authenticated!";
    }
}