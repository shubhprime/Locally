package com.locally.backend.service;

import com.locally.backend.dto.AppResponse;
import com.locally.backend.dto.LoginRequest;

public interface LoginService {
    AppResponse loginUser(LoginRequest loginRequest);
}