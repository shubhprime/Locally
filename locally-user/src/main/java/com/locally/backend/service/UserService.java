package com.locally.backend.service;

import com.locally.backend.dto.AppResponse;
import com.locally.backend.dto.UserRequest;

public interface UserService {
    AppResponse createUser(UserRequest userRequest);

    AppResponse sendVerificationOtp(UserRequest userRequest);
}