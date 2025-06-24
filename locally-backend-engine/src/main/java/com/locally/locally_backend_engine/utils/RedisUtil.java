package com.locally.locally_backend_engine.utils;

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
    private static final String STATUS_PREFIX = "status:";

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

    /**
     * Set availability status in Redis with a TTL. With TTL
     */
    public void setDeliveryPartnerStatus(String email, String status, long durationMinutes) {
        String key = STATUS_PREFIX + email;
        try {
            redisTemplate.opsForValue().set(key, status, durationMinutes, TimeUnit.MINUTES);
            System.out.println("[RedisUtil] Set status: " + key + " = " + status);
        } catch (Exception e) {
            System.out.println("[RedisUtil] Error setting status: " + e.getMessage());
        }
    }

    /**
     * Set availability status in Redis with a TTL. Without TTL
     */
    public void setDeliveryPartnerStatus(String email, String status) {
        String key = "status:" + email;
        redisTemplate.opsForValue().set(key, status);
    }

    /**
     * Get availability status from Redis.
     */
    public String getDeliveryPartnerStatus(String email) {
        String key = STATUS_PREFIX + email;
        try {
            String status = redisTemplate.opsForValue().get(key);
            System.out.println("[RedisUtil] Get status: " + key + " => " + status);
            return status;
        } catch (Exception e) {
            System.out.println("[RedisUtil] Error getting status: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete availability status explicitly (optional).
     */
    public void clearDeliveryPartnerStatus(String email) {
        String key = STATUS_PREFIX + email;
        try {
            redisTemplate.delete(key);
            System.out.println("[RedisUtil] Cleared status: " + key);
        } catch (Exception e) {
            System.out.println("[RedisUtil] Error clearing status: " + e.getMessage());
        }
    }

    public void setWithExpiry(String key, String value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}