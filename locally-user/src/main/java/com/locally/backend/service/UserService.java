package com.locally.backend.service;

import com.locally.backend.dto.*;

public interface UserService {
    public AppResponse createUser(UserRequest userRequest);

    public AppResponse uploadProfilePicture(UploadProfilePictureRequest uploadProfilePictureRequest);

    public AppResponse updateUserProfile(UpdateUserRequest updateUserRequest);

    public AppResponse loginUser(LoginRequest loginRequest);

    public AppResponse logoutUser(RefreshTokenRequest logoutRequest);

    public AppResponse deleteAccount(DeleteUserRequest deleteUserRequest);

    public AppResponse sendVerificationOtp(SendVerificationOtpRequest sendVerificationOtpRequest);

    public AppResponse sendVerificationOtp(String email);

    public AppResponse verifyVerificationOtp(OtpVerificationRequest otpVerificationRequest);
}