package com.locally.locally_delivery_partner.service.impl;

import com.locally.locally_delivery_partner.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class OtpServiceImpl implements OtpService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void storeOtp(String key, String otp, long expirySeconds) {
        redisTemplate.opsForValue().set(key, otp, Duration.ofSeconds(expirySeconds));
    }

    @Override
    public String getOtp(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean validateOtp(String key, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(key);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    @Override
    public void deleteOtp(String key) {
        redisTemplate.delete(key);
    }
}