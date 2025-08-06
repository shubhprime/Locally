package com.locally.locally_delivery_partner.service;

public interface OtpService {

    void storeOtp(String key, String otp, long expirySeconds);

    String getOtp(String key);

    boolean validateOtp(String key, String otp);

    void deleteOtp(String key);
}