package com.locally.backend.service;

public interface EmailService {
    void sendVerificationOtp(String to, String otp);
    void sendWelcomeEmail(String to, String name);
    void sendForgotPasswordOtp(String to, String otp);
}