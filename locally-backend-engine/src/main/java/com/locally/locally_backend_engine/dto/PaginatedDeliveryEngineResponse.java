package com.locally.locally_backend_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedDeliveryEngineResponse {
    private List<AllUserDeliveryEngineResponse> deliveries;
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;
}
