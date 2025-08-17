package com.locally.backend.controller.auth;

import com.locally.backend.dto.*;
import com.locally.backend.service.UserService;
import com.locally.backend.utils.AccountUtils;
import com.locally.backend.utils.JwtUtil;
import com.locally.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user/v1/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/signup")
    public ResponseEntity<AppResponse> createUser(@RequestBody UserRequest userRequest) {
        AppResponse appResponse = userService.createUser(userRequest);

        if (appResponse.getResponseCode().equals(AccountUtils.ACCOUNT_EXISTS_CODE)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(appResponse); // 409 Conflict if account exists
        }

        if (appResponse.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(appResponse); // 201 Created
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(appResponse); // Generic fallback
        }
    }

    @PostMapping("/send-verification-otp")
    public ResponseEntity<AppResponse> sendVerificationOtp(@RequestBody SendVerificationOtpRequest sendVerificationOtpRequest) {
        AppResponse response = userService.sendVerificationOtp(sendVerificationOtpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/verify-verification-otp")
    public ResponseEntity<AppResponse> verifyVerificationOtp(@RequestBody OtpVerificationRequest request) {
        AppResponse appResponse = userService.verifyVerificationOtp(request);
        return ResponseEntity.status(HttpStatus.OK).body(appResponse);
    }

//    @PostMapping("/upload-profile-picture")
//    public ResponseEntity<AppResponse> uploadProfilePicture(@RequestParam("picture") MultipartFile picture, @RequestHeader("Authorization") String authHeader) {
//
//        String token = authHeader.substring(7);
//        String email = jwtUtil.retrieveSubject(token);
//
//        UploadProfilePictureRequest uploadProfilePictureRequest = new UploadProfilePictureRequest(email, picture);
//
//        AppResponse appResponse = userService.uploadProfilePicture(uploadProfilePictureRequest);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(appResponse);
//    }

    @PatchMapping("/update-profile")
    public ResponseEntity<AppResponse> updateProfile(@RequestBody UpdateUserRequest updateUserRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        updateUserRequest.setEmail(email);

        AppResponse appResponse = userService.updateUserProfile(updateUserRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(appResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AppResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        AppResponse appResponse = userService.loginUser(loginRequest);

        if (appResponse.isSuccess()) {

            // Build response with both tokens
            AppResponse responseWithTokens = AppResponse.builder()
                    .responseCode(appResponse.getResponseCode())
                    .success(true)
                    .responseMessage(appResponse.getResponseMessage())
                    .accessToken(appResponse.getAccessToken())           // access token
                    .refreshToken(appResponse.getRefreshToken())        // refresh token
                    .data(appResponse.getData())
                    .build();

            return ResponseEntity.ok(responseWithTokens);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(appResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<AppResponse> logoutUser(@RequestBody RefreshTokenRequest userRequest) {
        AppResponse appResponse = userService.logoutUser(userRequest);

        if (appResponse.isSuccess()) {
            return ResponseEntity.ok(appResponse);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(appResponse);
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<AppResponse> deleteUser(@RequestBody DeleteUserRequest deleteUserRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        deleteUserRequest.setEmail(email);

        AppResponse appResponse = userService.deleteAccount(deleteUserRequest);
        if (appResponse.isSuccess()) {
            return ResponseEntity.ok(appResponse);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(appResponse);
        }
    }

    // TODO: Remove this route (ONLY FOR TESTING JWT AUTHORIZATION)
    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "This is a protected resource. You are authenticated!";
    }
}