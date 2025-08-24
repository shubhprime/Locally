package com.locally.locally_backend_engine.config;

import com.locally.locally_backend_engine.utils.JwtUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.requests-per-minute:300}") // Increased limit for MVP
    private int requestsPerMinute;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // QUICK FIX 1: Skip rate limiting for internal service endpoints
        String requestPath = request.getRequestURI();
        if (requestPath.contains("/api/engine/")) {
            log.debug("Internal engine API call, bypassing rate limit: {}", requestPath);
            return true;
        }

        // QUICK FIX 2: More lenient service token check
        if (isServiceCall(request)) {
            log.debug("Valid service call detected, bypassing rate limit for path: {}", requestPath);
            return true;
        }

        // For non-service calls, apply rate limiting
        String clientKey = getClientKey(request);
        Bucket bucket = cache.computeIfAbsent(clientKey, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            return true;
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for key: {} on path: {}", clientKey, requestPath);

            response.setStatus(429);
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("Retry-After", "60");

            try {
                response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again in 1 minute.\"}");
                response.setContentType("application/json");
            } catch (Exception e) {
                log.error("Error writing rate limit response: {}", e.getMessage());
            }

            return false;
        }
    }

    private boolean isServiceCall(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // More lenient check - if it looks like a service token, allow it
                if (token.length() > 50) { // Service tokens are typically longer
                    boolean isValid = jwtUtil.isServiceTokenValid(token);
                    log.debug("Service token validation result: {} for token length: {}", isValid, token.length());
                    return isValid;
                }
            }
        } catch (Exception e) {
            log.warn("Error checking service token, allowing request: {}", e.getMessage());
            // QUICK FIX 3: If service token check fails, allow the request (for MVP)
            return true;
        }
        return false;
    }

    private String getClientKey(HttpServletRequest request) {
        // QUICK FIX 4: Use a combination of IP and User-Agent for better uniqueness
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        return clientIp + "_" + (userAgent != null ? userAgent.hashCode() : "unknown");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private Bucket createNewBucket(String clientKey) {
        // QUICK FIX 5: More generous rate limiting for MVP
        Bandwidth bandwidth = Bandwidth.classic(requestsPerMinute,
                Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    // QUICK FIX 6: Add cache cleanup to prevent memory issues
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupCache() {
        if (cache.size() > 1000) { // If cache gets too big
            log.info("Cleaning up rate limit cache, current size: {}", cache.size());
            cache.clear(); // Simple cleanup for MVP
        }
    }
}