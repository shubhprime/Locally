package com.locally.locally_backend_engine.config;

import com.locally.locally_backend_engine.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //  stateless config
                )
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/engine/v1/user/delivery/create-delivery",
                                "/api/engine/v1/user/delivery/get-all-user-delivery",
                                "/api/engine/v1/user/delivery/get-user-delivery-by-id",
                                "/api/engine/v1/user/delivery/update-delivery",
                                "/api/engine/v1/user/delivery/cancel-delivery",
                                "/api/engine/v1/delivery-partner/delivery/accept-delivery",
                                "/api/engine/v1/delivery-partner/delivery/mark-delivery-in-transit",
                                "/api/engine/v1/delivery-partner/delivery/mark-delivery-delivered",
                                "/api/engine/v1/delivery-partner/delivery/mark-delivery-paid",
                                "/api/engine/v1/delivery-partner/delivery/cancel-delivery",
                                "/api/engine/v1/delivery-partner/delivery/delivery-failed",
                                "/api/engine/v1/delivery-partner/delivery/get-active-delivery/{partnerId}",
                                "/api/engine/v1/location/update",
                                "/api/engine/v1/tracking/assign-delivery",
                                "/api/engine/v1/webhook/stripe")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable());
        return http.build();
    }
}