package com.locally.locally_backend_engine.config;

import com.locally.locally_backend_engine.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        String authHeader = request.getHeader(AUTH_HEADER);

        // Check if authorization header exists
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            sendUnauthorizedResponse(response, "Missing or invalid authorization header");
            return false;
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX.length());

            // Validate token
            if (!jwtUtil.isServiceTokenValid(token)) {
                sendUnauthorizedResponse(response, "Invalid or expired token");
                return false;
            }

            // Extract and validate user ID
            Long userId = jwtUtil.retrieveUserIdFromServiceToken(token);
            if (userId == null || userId <= 0) {
                sendUnauthorizedResponse(response, "Invalid user identification");
                return false;
            }

            // Add user ID to request attributes for later use
            request.setAttribute("userId", userId);

            // Log successful authentication

            return true;

        } catch (Exception e) {
            sendUnauthorizedResponse(response, "Authentication failed");
            return false;
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                message, java.time.Instant.now()
        ));
    }

    private String getClientIp(HttpServletRequest request) {
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
}