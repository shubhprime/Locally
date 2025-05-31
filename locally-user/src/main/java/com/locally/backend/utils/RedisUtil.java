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

    private static final String DEBUG_BLACKLIST_PREFIX = "blacklist:";

    // Store the token with expiry equal to JWT expiration
    public void blacklistToken(String token, Date expirationDate) {
        long ttlMillis = expirationDate.getTime() - System.currentTimeMillis();
        String key = DEBUG_BLACKLIST_PREFIX + token;
        System.out.println("[RedisUtil] Blacklisting token: " + key);
        System.out.println("[RedisUtil] Expiration in ms: " + ttlMillis);

        if (ttlMillis > 0) {
            try {
                redisTemplate.opsForValue().set(key, "blacklisted", ttlMillis, TimeUnit.MILLISECONDS);
                System.out.println("[RedisUtil] Token blacklisted successfully.");
            } catch (Exception e) {
                System.out.println("[RedisUtil] Error while blacklisting token: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[RedisUtil] Token already expired. Not blacklisting.");
        }
    }

    public boolean isTokenBlacklisted(String token) {
//        return redisTemplate.hasKey(token);
        String key = DEBUG_BLACKLIST_PREFIX + token;
        try {
            Boolean exists = redisTemplate.hasKey(key);
            System.out.println("[RedisUtil] Checking token blacklist status: " + key + " => " + exists);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            System.out.println("[RedisUtil] Error while checking blacklist status: " + e.getMessage());
            e.printStackTrace();
            return false; // Default to "not blacklisted" if Redis fails
        }
    }
}