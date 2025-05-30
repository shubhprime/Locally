package com.locally.backend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Store the token with expiry equal to JWT expiration
    public void blacklistToken(String token, Date expirationDate) {
        long ttlMillis = expirationDate.getTime() - System.currentTimeMillis();
        if (ttlMillis > 0) {
            redisTemplate.opsForValue().set(token, "blacklisted", ttlMillis, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}