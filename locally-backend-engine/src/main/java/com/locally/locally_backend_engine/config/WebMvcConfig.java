package com.locally.locally_backend_engine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableScheduling
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitingInterceptor rateLimitingInterceptor;

    @Autowired
    private SecurityInterceptor securityInterceptor;

    @Autowired
    private AuditInterceptor auditInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Security interceptor - runs first
        registry.addInterceptor(securityInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/engine/webhook/**",
                        "/api/v1/engine/health/**"
                );

        // Rate limiting interceptor - runs second
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/engine/webhook/**",
                        "/api/v1/engine/health/**"
                );

        // Audit interceptor - runs last to capture all processed requests
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/engine/health/**",
                        "/api/v1/engine/metrics/**"
                );
    }
}