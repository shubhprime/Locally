package com.locally.locally_backend_engine.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    @Value("${app.rate-limit.enabled:false}") // Disabled by default for MVP
    private boolean rateLimitEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // MVP: Rate limiting is disabled
        if (!rateLimitEnabled) {
            log.debug("Rate limiting disabled for MVP - allowing request to: {}", request.getRequestURI());
            return true;
        }

        // For future production use - just return true for now
        log.debug("Rate limiting bypassed for path: {}", request.getRequestURI());
        return true;
    }
}