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

    private final ConcurrentHashMap<String, Long> lastAccessTime = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    @Value("${app.rate-limit.cache-cleanup-interval:300000}")
    private long cacheCleanupInterval;

    @Value("${app.rate-limit.cache-entry-ttl:600000}")
    private long cacheEntryTtl;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientId = getClientId(request);

        // Update last access time
        lastAccessTime.put(clientId, System.currentTimeMillis());

        Bucket bucket = cache.computeIfAbsent(clientId, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            return true;
        } else {
            response.setStatus(429); // Too Many Requests
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("Retry-After", "60"); // Retry after 1 minute

            log.warn("Rate limit exceeded for client: {} on path: {}", clientId, request.getRequestURI());

            try {
                response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}");
                response.setContentType("application/json");
            } catch (Exception e) {
                log.error("Error writing rate limit response: {}", e.getMessage());
            }

            return false;
        }
    }

    private String getClientId(HttpServletRequest request) {
        // Priority order for client identification:

        // 1. Try to get service token first (for service-to-service calls)
        String serviceToken = extractServiceTokenFromRequest(request);
        if (serviceToken != null) {
            return "service:" + serviceToken; // Service calls get highest limits
        }

        // 2. Try to get user ID from JWT token (if available)
        String userId = extractUserIdFromToken(request);
        if (userId != null) {
            return "user:" + userId;
        }

        // 3. Try to get API key from headers
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api:" + apiKey;
        }

        // 4. Try to get device ID from headers (for mobile apps)
        String deviceId = request.getHeader("X-Device-ID");
        if (deviceId != null && !deviceId.isEmpty()) {
            return "device:" + deviceId;
        }

        // 5. Fall back to IP address
        String clientIp = getClientIpAddress(request);
        return "ip:" + clientIp;
    }

    private String extractUserIdFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Here you would decode the JWT token and extract user ID
                // This is a placeholder - implement according to your JWT setup
                // String token = authHeader.substring(7);
                // return jwtService.getUserIdFromToken(token);
                return null; // Placeholder
            }
        } catch (Exception e) {
            log.debug("Failed to extract user ID from token: {}", e.getMessage());
        }
        return null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String extractServiceTokenFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // Use your existing JWT utility to check if it's a service token
                if (jwtUtil.isServiceTokenValid(token)) {
                    // You could return the service name or just a generic identifier
                    return "internal-service";
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract service token: {}", e.getMessage());
        }
        return null;
    }

    private Bucket createNewBucket(String clientId) {
        // Different limits based on client type
        int limit = requestsPerMinute;

        if (clientId.startsWith("service:")) {
            // Service-to-service calls get very high limits
            limit = requestsPerMinute * 10; // Much higher for internal services
        } else if (clientId.startsWith("user:")) {
            // Authenticated users get higher limits
            limit = requestsPerMinute * 2;
        } else if (clientId.startsWith("api:")) {
            // API keys get even higher limits
            limit = requestsPerMinute * 5;
        } else if (clientId.startsWith("ip:")) {
            // IP-based limits are more restrictive
            limit = requestsPerMinute / 2;
        }

        Bandwidth bandwidth = Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}