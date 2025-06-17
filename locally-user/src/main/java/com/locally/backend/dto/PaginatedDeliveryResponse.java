package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedDeliveryResponse {
    private List<AllUserDeliveryResponse> deliveries;
    private int currentPage = 0;
    private int pageSize = 5;
    private long totalItems = 0L;
    private int totalPages = 0;
}