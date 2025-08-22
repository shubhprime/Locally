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

    @Value("${app.rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // Check if it's a service token - if so, bypass rate limiting
        if (isServiceCall(request)) {
            log.debug("Service call detected, bypassing rate limit for path: {}", request.getRequestURI());
            return true;
        }

        // For non-service calls, apply rate limiting based on IP
        String clientIp = getClientIpAddress(request);
        Bucket bucket = cache.computeIfAbsent(clientIp, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            return true;
        } else {
            // Rate limit exceeded
            response.setStatus(429);
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("Retry-After", "60");

            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, request.getRequestURI());

            try {
                response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}");
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
                return jwtUtil.isServiceTokenValid(token);
            }
        } catch (Exception e) {
            log.debug("Error checking service token: {}", e.getMessage());
        }
        return false;
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

    private Bucket createNewBucket(String clientIp) {
        // Simple rate limiting: 100 requests per minute per IP
        Bandwidth bandwidth = Bandwidth.classic(requestsPerMinute, Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}