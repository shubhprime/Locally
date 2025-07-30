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
public class AddressListResponse {
    private String responseCode;
    private boolean success;
    private String responseMessage;
    private List<AddressResponse> addresses;
    private long totalCount;
}