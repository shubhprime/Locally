package com.locally.locally_backend_engine.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AuditInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        Object userId = request.getAttribute("userId");

        // Log all wallet operations for audit purposes
        if (path.contains("/wallet/")) {
            log.info("AUDIT: {} {} for user: {}", method, path, userId);
        }

        // Add request start time for performance monitoring
        request.setAttribute("startTime", System.currentTimeMillis());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        int status = response.getStatus();
        Object userId = request.getAttribute("userId");

        Long startTime = (Long) request.getAttribute("startTime");
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

        // Log completion with performance metrics
        if (path.contains("/wallet/")) {
            if (ex != null) {
                log.error("AUDIT: {} {} completed with error for user: {} - Status: {} Duration: {}ms Error: {}",
                        method, path, userId, status, duration, ex.getMessage());
            } else {
                log.info("AUDIT: {} {} completed for user: {} - Status: {} Duration: {}ms",
                        method, path, userId, status, duration);
            }
        }

        // Alert on slow requests
        if (duration > 5000) { // 5 seconds
            log.warn("PERFORMANCE: Slow request detected - {} {} took {}ms for user: {}",
                    method, path, duration, userId);
        }
    }
}