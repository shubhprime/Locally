package com.locally.backend.utils;

import java.security.SecureRandom;

public class OtpUtil {
    public static String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }
}