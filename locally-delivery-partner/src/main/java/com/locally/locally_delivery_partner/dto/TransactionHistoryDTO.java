package com.locally.locally_delivery_partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryDTO {
    private List<TransactionDTO> transactions;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}