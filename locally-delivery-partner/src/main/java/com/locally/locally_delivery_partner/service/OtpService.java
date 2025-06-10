package com.locally.locally_delivery_partner.service;

public interface OtpService {

    public void storeOtp(String key, String otp, long expirySeconds);

    public String getOtp(String key);

    boolean validateOtp(String key, String otp);

    public void deleteOtp(String key);
}