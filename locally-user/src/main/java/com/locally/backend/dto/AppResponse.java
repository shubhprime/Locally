package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppResponse<T> {
    private String responseCode;
    private boolean success;
    private String responseMessage;
    private String accessToken;
    private String refreshToken;
    private T data;
}