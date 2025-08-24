package com.locally.locally_backend_engine.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AuditInterceptor implements HandlerInterceptor {

    @Value("${app.audit.enabled:true}") // Keep basic auditing for MVP
    private boolean auditEnabled;

    @Value("${app.audit.performance-monitoring:false}") // Disable detailed performance monitoring
    private boolean performanceMonitoringEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!auditEnabled) {
            return true;
        }

        // Only log important wallet operations for MVP
        String path = request.getRequestURI();
        if (path.contains("/wallet/add-money") || path.contains("/wallet/withdraw-money")) {
            log.info("WALLET_OP: {} {}", request.getMethod(), path);
        }

        // Add start time only if performance monitoring is enabled
        if (performanceMonitoringEnabled) {
            request.setAttribute("startTime", System.currentTimeMillis());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!auditEnabled) {
            return;
        }

        String path = request.getRequestURI();

        // Only log errors for important operations
        if (ex != null && (path.contains("/wallet/") || path.contains("/transaction/"))) {
            log.error("ERROR: {} {} failed - Status: {} Error: {}",
                    request.getMethod(), path, response.getStatus(), ex.getMessage());
        }

        // Basic performance monitoring only if enabled
        if (performanceMonitoringEnabled) {
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;

                // Only alert on very slow requests (10 seconds for MVP)
                if (duration > 10000) {
                    log.warn("SLOW_REQUEST: {} {} took {}ms", request.getMethod(), path, duration);
                }
            }
        }
    }
}