package com.locally.backend.dto;

import java.time.LocalDateTime;

public class TrackingResponse {
    private String deliveryStatus;
    private double latitude;
    private double longitude;
    private LocalDateTime lastUpdated;
}