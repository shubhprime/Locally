package com.locally.backend.service;

import com.locally.backend.dto.*;

public interface UserService {
    AppResponse createUser(UserRequest userRequest);

//    AppResponse uploadProfilePicture(UploadProfilePictureRequest uploadProfilePictureRequest);

    AppResponse updateUserProfile(UpdateUserRequest updateUserRequest);

    AppResponse loginUser(LoginRequest loginRequest);

    AppResponse logoutUser(RefreshTokenRequest logoutRequest);

    AppResponse deleteAccount(DeleteUserRequest deleteUserRequest);

    AppResponse sendVerificationOtp(SendVerificationOtpRequest sendVerificationOtpRequest);

    AppResponse sendVerificationOtp(String email);

    AppResponse verifyVerificationOtp(OtpVerificationRequest otpVerificationRequest);
}